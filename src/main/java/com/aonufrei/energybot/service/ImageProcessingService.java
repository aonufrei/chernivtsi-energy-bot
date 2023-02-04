package com.aonufrei.energybot.service;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ImageProcessingService {

	private final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);
	private static final String OBL_ENERGO_URL = "https://oblenergo.cv.ua/shutdowns/GPV.png";

	public byte[] getImageBytes() {
		URL url;
		try {
			url = new URL(OBL_ENERGO_URL);
		} catch (MalformedURLException e) {
			log.error("Url is not valid", e);
			return null;
		}
		try (InputStream in = new BufferedInputStream(url.openStream())) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(in, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			log.error("Error occurred when processing image");
			return null;
		}
	}

	public BufferedImage mergeImagesVertically(List<BufferedImage> images) {
		int height = images.stream().mapToInt(BufferedImage::getHeight).sum();
		int width = images.get(0).getWidth();
		BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics g = finalImage.getGraphics();
		int y = 0;
		for (BufferedImage image : images) {
			g.drawImage(image, 0, y, null);
			y += image.getHeight();
		}
		g.dispose();

		return finalImage;
	}

	public String hashImageData(byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		md.update(data);
		return Hex.encodeHexString(md.digest());
	}
}
