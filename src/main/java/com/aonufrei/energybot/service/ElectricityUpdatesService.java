package com.aonufrei.energybot.service;

import com.aonufrei.energybot.dto.ScheduleProcessingResponse;
import com.aonufrei.energybot.exceptions.FetchException;
import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.assertthat.selenium_shutterbug.core.Snapshot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ElectricityUpdatesService {

	private final Logger log = LoggerFactory.getLogger(ElectricityUpdatesService.class);

	private final ImageProcessingService imageProcessingService;

	private final MailingService mailingService;

	private final HashService hashService;

	private final String SHUTDOWNS_URL = "https://oblenergo.cv.ua/shutdowns/";

	@Value("${chromedriver.path}")
	private String CHROMEDRIVER_LOCATION;

	public ElectricityUpdatesService(ImageProcessingService imageProcessingService, MailingService mailingService, HashService hashService) {
		this.imageProcessingService = imageProcessingService;
		this.mailingService = mailingService;
		this.hashService = hashService;
	}

	@Deprecated
	public void detectChanges() {
		log.info("Get image bytes");
		byte[] imageBytes = imageProcessingService.getImageBytes();
		if (imageBytes == null) {
			return;
		}
		log.info("Notifying subscribers");
		mailingService.notifySubscribers(imageBytes);
	}

	public ScheduleProcessingResponse processChanges() {
		log.info("Get table hash");
		String hash;
		byte[] screenshot;
		try {
			hash = currentTableHash();
			screenshot = takeScheduleScreenshot();
		} catch (FetchException fe) {
			log.error("Failed to get table hash or to take a screenshot", fe);
			mailingService.sendAdminMessage("Failed to get table hash or to take a screenshot");
			return null;
		} catch (IOException e) {
			log.error("Failed to connect to shutdowns webpage", e);
			mailingService.sendAdminMessage("Failed to connect to shutdowns webpage");
			return null;
		}
		return new ScheduleProcessingResponse(hash, screenshot);
	}

	public void processChangesAndSendNotifications() {
		ScheduleProcessingResponse processingResponse = processChanges();
		if (processingResponse == null) {
			return;
		}
		log.info("Notifying subscribers");
		mailingService.notifySubscribers(processingResponse.getHash(), processingResponse.getImage());
	}

	public String currentTableHash() throws IOException, FetchException {
		String shutdownsValues = parseTableValueFromWebpage();
		return hashService.hashText(shutdownsValues);
	}

	public byte[] takeScheduleScreenshot() throws FetchException {
		System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_LOCATION);
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless", "--window-size=1920,1080");
		WebDriver driver = new ChromeDriver(chromeOptions);
		driver.get(SHUTDOWNS_URL);
		BufferedImage screenshotOfTable = getScreenshotOfTable(driver);
		driver.quit();
		try {
			return toByteArray(screenshotOfTable, "png");
		} catch (IOException e) {
			return null;
		}
	}

	public BufferedImage getScreenshotOfTable(WebDriver driver) throws FetchException {
		var elementsToTakeScreenshot = Arrays.asList(
				driver.findElement(By.id("gsv_e")),
				driver.findElement(By.id("gsv_h")),
				driver.findElement(By.id("gsv"))
		);

		if (elementsToTakeScreenshot.stream().anyMatch(Objects::isNull)) {
			throw new FetchException("On of required ui elements are missing");
		}

		List<BufferedImage> images = elementsToTakeScreenshot.stream()
				.map(it -> Shutterbug.shootElement(driver, it))
				.map(Snapshot::getImage)
				.collect(Collectors.toList());
		return imageProcessingService.mergeImagesVertically(images);
	}

	public byte[] getFullPageScreenshot() {
		System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_LOCATION);
		WebDriver driver = new ChromeDriver();
		try {
			driver.get(SHUTDOWNS_URL);
			driver.manage().window().setSize(new Dimension(1024, 1000));
			var finalScreenshot = Shutterbug.shootPage(driver, Capture.FULL).getImage();
			return toByteArray(finalScreenshot, "png");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			driver.quit();
		}
	}

	public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bi, format, baos);
		return baos.toByteArray();
	}

	public String parseTableValueFromWebpage() throws IOException, FetchException {
		Document doc = Jsoup.connect(SHUTDOWNS_URL).get();
		Element table = doc.getElementById("gsv");
		if (table == null) {
			throw new FetchException("Table was not found on the webpage");
		}
		Elements ps = table.getElementsByTag("p");
		if (ps.size() < 1) {
			throw new FetchException("No date tag was found in the table");
		}
		var data = new StringBuilder();
		String date = ps.get(0).html();
		data.append(date);
		for (int i = 1; i <= 18; i++) {
			String rowId = String.format("inf%d", i);
			Element row = table.getElementById(rowId);
			if (row == null) {
				throw new FetchException("Cannot find row " + rowId);
			}
			String rowData = row.children().stream()
					.map(Element::tagName)
					.collect(Collectors.joining());
			data.append(rowData);
		}
		return data.toString();
	}


}
