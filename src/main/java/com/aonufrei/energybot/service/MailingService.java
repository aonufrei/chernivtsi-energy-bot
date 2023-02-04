package com.aonufrei.energybot.service;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.model.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailingService {

	private final Logger log = LoggerFactory.getLogger(MailingService.class);

	private final SubscribersService subscribersService;

	private final PollingBot pollingBot;

	private final HashService hashService;

	@Value("${admin.chatid}")
	public String ADMIN_CHAT_ID;

	public MailingService(SubscribersService subscribersService, PollingBot pollingBot, HashService hashService) {
		this.subscribersService = subscribersService;
		this.pollingBot = pollingBot;
		this.hashService = hashService;
	}

	public void sendCurrentSchedule(String chatId, byte[] imageBytes) {
		String currentImageHash = hashService.hashData(imageBytes);
		sendCurrentSchedule(chatId, currentImageHash, imageBytes);
	}

	public void sendCurrentSchedule(String chatId, String currentImageHash, byte[] imageBytes) {
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

	public void sendAdminMessage(String messageContent) {
		SendMessage message = SendMessage.builder()
				.chatId(ADMIN_CHAT_ID)
				.text(messageContent)
				.build();
		try {
			pollingBot.executeAsync(message);
		} catch (TelegramApiException e) {
			log.error("Failed to send admin message", e);
		}
	}

	public void notifySubscribers(byte[] imageBytes) {
		String currentImageHash = hashService.hashData(imageBytes);
		notifySubscribers(currentImageHash, imageBytes);
	}

	public void notifySubscribers(String hash, byte[] image) {
		log.info("Sending updates to subscribers");
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(image)) {
			InputFile attachment = new InputFile();
			attachment.setMedia(inputStream, "Schedule");
			notifySubscribers(attachment, hash);
		} catch (IOException e) {
			log.info("Error when sending image to subscribers");
		}
	}

	public void notifySubscribers(InputFile updatedImage, String currentHash) {
		List<Subscriber> subscriberToNotify = subscribersService.getSubscribersThatDidntReceive(currentHash).stream()
				.filter(s -> ADMIN_CHAT_ID.equals(s.getChatId()))
				.collect(Collectors.toList());
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
