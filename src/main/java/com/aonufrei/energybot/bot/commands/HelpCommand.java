package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {

	private static final Logger log = LoggerFactory.getLogger(HelpCommand.class);

	private List<CommandInfo> availableCommands = new LinkedList<>();

	@Override
	public CommandInfo getCommandInfo() {
		return new CommandInfo("/help", "Returns list of available commands");
	}

	@Override
	public void process(PollingBot pollingBot, Update update) {
		String response = availableCommands.stream()
				.map(this::outputCommandInfo)
				.collect(Collectors.joining("\n"));
		SendMessage message = SendMessage.builder()
				.chatId(update.getMessage().getChatId().toString())
				.text(response)
				.build();
		try {
			pollingBot.execute(message);
		} catch (TelegramApiException e) {
			log.error("Unable to send message to telegram user", e);
		}
	}

	private String outputCommandInfo(CommandInfo commandInfo) {
		return String.format("%s - %s", commandInfo.getName(), commandInfo.getDescription());
	}

	public void setAvailableCommands(List<CommandInfo> availableCommands) {
		this.availableCommands = availableCommands;
	}
}
