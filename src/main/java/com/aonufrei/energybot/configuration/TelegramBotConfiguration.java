package com.aonufrei.energybot.configuration;

import com.aonufrei.energybot.dto.BotInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfiguration {

	@Value("${telegram.token}")
	private String botToken;

	@Value("${telegram.bot-name}")
	private String botName;

	@Bean
	public BotInfo getBotInfo() {
		return new BotInfo(botName, botToken);
	}
}
