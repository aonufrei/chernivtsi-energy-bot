package com.aonufrei.energybot;

import com.aonufrei.energybot.bot.PollingBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class ChernivtsiEnergyBotApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(ChernivtsiEnergyBotApplication.class);

	@Autowired
	private PollingBot pollingBot;

	public static void main(String[] args) {
		SpringApplication.run(ChernivtsiEnergyBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(pollingBot);
		log.info("Bot is up");
	}
}
