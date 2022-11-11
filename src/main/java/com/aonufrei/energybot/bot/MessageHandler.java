package com.aonufrei.energybot.bot;

import com.aonufrei.energybot.bot.commands.*;
import com.aonufrei.energybot.service.ImageProcessingService;
import com.aonufrei.energybot.service.MailingService;
import com.aonufrei.energybot.service.SubscribersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

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

	public MessageHandler(SubscribersService subscribersService, PollingBot pollingBot, ImageProcessingService imageProcessingService, MailingService mailingService) {
		this.subscribersService = subscribersService;
		this.pollingBot = pollingBot;
		this.imageProcessingService = imageProcessingService;
		this.mailingService = mailingService;
	}

	@PostConstruct
	public void init() {
		pollingBot.setMessageHandler(this);
		HelpCommand helpCommand = new HelpCommand();
		StartCommand startCommand = new StartCommand(subscribersService);
		CurrentScheduleCommand currentScheduleCommand = new CurrentScheduleCommand(imageProcessingService, mailingService);
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
				log.info("Unknown command was triggered");
			}
		}
	}

}
