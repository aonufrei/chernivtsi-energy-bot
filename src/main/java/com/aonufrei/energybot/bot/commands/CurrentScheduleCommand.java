package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import com.aonufrei.energybot.service.ImageProcessingService;
import com.aonufrei.energybot.service.MailingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CurrentScheduleCommand extends AbstractCommand {

	private final Logger log = LoggerFactory.getLogger(MailingService.class);
	private final ImageProcessingService imageProcessingService;
	private final MailingService mailingService;

	public CurrentScheduleCommand(ImageProcessingService imageProcessingService, MailingService mailingService) {
		this.imageProcessingService = imageProcessingService;
		this.mailingService = mailingService;
	}

	@Override
	public CommandInfo getCommandInfo() {
		return new CommandInfo("/schedule", "Sends the current schedule");
	}

	@Override
	public void process(PollingBot pollingBot, Update update) {
		String chatId = update.getMessage().getChatId().toString();
//		byte[] imageBytes = imageProcessingService.getImageBytes();
//		mailingService.sendCurrentSchedule(chatId, imageBytes);
		SendMessage message = SendMessage.builder()
				.chatId(chatId)
				.text("Sorry, the service is not available at the moment. " +
						"The oblenergo's website has changed and now we cannot get image of the schedule. " +
						"We need some time to adapt. You can find the current schedule on https://oblenergo.cv.ua/")
				.build();
		try {
			pollingBot.executeAsync(message);
		} catch (TelegramApiException e) {
			log.error("Failed to send not available message", e);
		}
	}
}
