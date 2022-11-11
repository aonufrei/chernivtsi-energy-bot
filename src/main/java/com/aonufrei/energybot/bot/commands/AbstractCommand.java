package com.aonufrei.energybot.bot.commands;

import com.aonufrei.energybot.bot.PollingBot;
import com.aonufrei.energybot.dto.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class AbstractCommand {

	private static final Logger log = LoggerFactory.getLogger(AbstractCommand.class);

	public abstract CommandInfo getCommandInfo();

	public void process(PollingBot pollingBot, Update update) {
		log.info("Command without action was triggered");
	}

}
