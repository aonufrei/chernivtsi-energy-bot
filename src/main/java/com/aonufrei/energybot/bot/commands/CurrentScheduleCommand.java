package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import com.aonufrei.energybot.dto.ScheduleProcessingResponse;
import com.aonufrei.energybot.service.ElectricityUpdatesService;
import com.aonufrei.energybot.service.ImageProcessingService;
import com.aonufrei.energybot.service.MailingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CurrentScheduleCommand extends AbstractCommand {

	private final Logger log = LoggerFactory.getLogger(MailingService.class);
	private final MailingService mailingService;
	private final ElectricityUpdatesService updatesService;

	public CurrentScheduleCommand(MailingService mailingService, ElectricityUpdatesService updatesService) {
		this.mailingService = mailingService;
		this.updatesService = updatesService;
	}

	@Override
	public CommandInfo getCommandInfo() {
		return new CommandInfo("/schedule", "Sends the current schedule");
	}

	@Override
	public void process(PollingBot pollingBot, Update update) {
		String chatId = update.getMessage().getChatId().toString();
		log.info("Received current schedule message from [" + chatId + "]");
		ScheduleProcessingResponse processingResponse = updatesService.processChanges();
		if (processingResponse == null) {
			mailingService.sendAdminMessage("Failed to get current schedule");
			try {
				SendMessage sm = SendMessage.builder()
						.chatId(chatId)
						.text("Sorry, something went wrong when getting current schedule. Please, try again later")
						.build();
				pollingBot.executeAsync(sm);
			} catch (TelegramApiException e) {
				log.error("Failed to send sorry message to user", e);
			}
			return;
		}
		mailingService.sendCurrentSchedule(chatId, processingResponse.getHash(), processingResponse.getImage());
	}
}
