package com.aonufrei.energybot.bot;

import com.aonufrei.energybot.bot.commands.*;
import com.aonufrei.energybot.service.ElectricityUpdatesService;
import com.aonufrei.energybot.service.ImageProcessingService;
import com.aonufrei.energybot.service.MailingService;
import com.aonufrei.energybot.service.SubscribersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageHandler {

	private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

	private final Map<String, AbstractCommand> commandRegistry = new LinkedHashMap<>();

	private final PollingBot pollingBot;
	private final SubscribersService subscribersService;
	private final ImageProcessingService imageProcessingService;
	private final MailingService mailingService;

	private final ElectricityUpdatesService updatesService;

	public MessageHandler(SubscribersService subscribersService, PollingBot pollingBot, ImageProcessingService imageProcessingService, MailingService mailingService, ElectricityUpdatesService updatesService) {
		this.subscribersService = subscribersService;
		this.pollingBot = pollingBot;
		this.imageProcessingService = imageProcessingService;
		this.mailingService = mailingService;
		this.updatesService = updatesService;
	}

	@PostConstruct
	public void init() {
		pollingBot.setMessageHandler(this);
		HelpCommand helpCommand = new HelpCommand();
		StartCommand startCommand = new StartCommand(subscribersService);
		CurrentScheduleCommand currentScheduleCommand = new CurrentScheduleCommand(imageProcessingService, mailingService, updatesService);
		UnsubscribeCommand unsubscribeCommand = new UnsubscribeCommand(subscribersService);

		List<AbstractCommand> availableCommands = new LinkedList<>();
		availableCommands.add(helpCommand);
		availableCommands.add(startCommand);
		availableCommands.add(currentScheduleCommand);
		availableCommands.add(unsubscribeCommand);

		availableCommands.forEach(c -> commandRegistry.put(c.getCommandInfo().getName(), c));
		helpCommand.setAvailableCommands(availableCommands.stream()
				.map(AbstractCommand::getCommandInfo)
				.collect(Collectors.toList()));
	}

	public void handleMessage(PollingBot pollingBot, Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String command = update.getMessage().getText();
			if (commandRegistry.containsKey(command)) {
				commandRegistry.get(command).process(pollingBot, update);
			} else {
				sendUnknownCommandMessage(update);
				log.info("Unknown command was triggered");
			}
		}
	}

	private void sendUnknownCommandMessage(Update update) {
		String chatId = update.getMessage().getChatId().toString();
		SendMessage sendMessage = SendMessage.builder()
				.chatId(chatId)
				.text("Unknown command was provided")
				.build();
		try {
			pollingBot.execute(sendMessage);
		} catch (TelegramApiException e) {
			log.error("Cannot send message to the user", e);
		}
	}

}
