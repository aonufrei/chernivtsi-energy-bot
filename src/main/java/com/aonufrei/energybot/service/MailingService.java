package com.aonufrei.energybot.service;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.model.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailingService {

	private final Logger log = LoggerFactory.getLogger(MailingService.class);

	private final SubscribersService subscribersService;

	private final ImageProcessingService imageProcessingService;

	private final PollingBot pollingBot;

	public MailingService(SubscribersService subscribersService, ImageProcessingService imageProcessingService, PollingBot pollingBot) {
		this.subscribersService = subscribersService;
		this.imageProcessingService = imageProcessingService;
		this.pollingBot = pollingBot;
	}

	public void sendCurrentSchedule(String chatId, byte[] imageBytes) {
		String currentImageHash = imageProcessingService.hashImageData(imageBytes);
		log.info(String.format("Sending schedule to [%s]", chatId));
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
			InputFile attachment = new InputFile();
			attachment.setMedia(inputStream, "Schedule");
			SendPhoto message = createNotification(chatId, "Current schedule", attachment);
			pollingBot.executeAsync(message);
			if (subscribersService.isChatSubscribed(chatId)) {
				Subscriber subscriber = subscribersService.getSubscriptionByChatId(chatId);
				subscriber.setLastImageReceivedHash(currentImageHash);
				subscribersService.save(subscriber);
			}
		} catch (IOException e) {
			log.info("Error when sending image to subscribers");
		}
	}

	public void notifySubscribers(byte[] imageBytes) {
		String currentImageHash = imageProcessingService.hashImageData(imageBytes);
		log.info("Sending updates to subscribers");
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
			InputFile attachment = new InputFile();
			attachment.setMedia(inputStream, "Schedule");
			notifySubscribers(attachment, currentImageHash);
		} catch (IOException e) {
			log.info("Error when sending image to subscribers");
		}
	}

	public void notifySubscribers(InputFile updatedImage, String currentHash) {
		List<Subscriber> subscriberToNotify = subscribersService.getSubscribersThatDidntReceive(currentHash);
		log.info(String.format("Sending updates to [%d] subscribers", subscriberToNotify.size()));
		subscriberToNotify.stream()
				.map(s -> createNotification(s.getChatId(), "Schedule was updated", updatedImage))
				.forEach(pollingBot::executeAsync);
		log.info("Saving new hashes to db");
		subscribersService.saveAll(subscriberToNotify.stream()
				.peek(el -> el.setLastImageReceivedHash(currentHash))
				.collect(Collectors.toList()));
	}

	private SendPhoto createNotification(String chartId, String caption, InputFile image) {
		return SendPhoto.builder()
				.chatId(chartId)
				.photo(image)
				.caption(caption)
				.build();
	}

}
