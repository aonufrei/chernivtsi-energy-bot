package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import com.aonufrei.energybot.service.ImageProcessingService;
import com.aonufrei.energybot.service.MailingService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CurrentScheduleCommand extends AbstractCommand {

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
		byte[] imageBytes = imageProcessingService.getImageBytes();
		mailingService.sendCurrentSchedule(chatId, imageBytes);
	}
}
