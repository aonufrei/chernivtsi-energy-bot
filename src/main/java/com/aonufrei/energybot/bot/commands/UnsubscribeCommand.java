package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import com.aonufrei.energybot.service.SubscribersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UnsubscribeCommand extends AbstractCommand {

	private static final Logger log = LoggerFactory.getLogger(UnsubscribeCommand.class);

	private final SubscribersService service;

	public UnsubscribeCommand(SubscribersService service) {
		this.service = service;
	}

	@Override
	public CommandInfo getCommandInfo() {
		return new CommandInfo("/unsubscribe", "Subscribes user to receive electricity updates");
	}

	@Override
	public void process(PollingBot pollingBot, Update update) {
		String chatId = update.getMessage().getChatId().toString();

		service.unsubscribeChat(chatId);
		SendMessage sendMessage = SendMessage.builder()
				.chatId(chatId)
				.text("You were successfully unsubscribed")
				.build();

		try {
			pollingBot.execute(sendMessage);
		} catch (TelegramApiException e) {
			log.error("Cannot send message to the user", e);
		}
	}
}
