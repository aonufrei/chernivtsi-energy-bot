package com.aonufrei.energybot.bot;

import com.aonufrei.energybot.dto.BotInfo;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class PollingBot extends TelegramLongPollingBot {

	private final BotInfo botInfo;

	private MessageHandler messageHandler;


	public PollingBot(BotInfo botInfo) {
		this.botInfo = botInfo;
	}

	@Override
	public String getBotUsername() {
		return botInfo.getName();
	}

	@Override
	public String getBotToken() {
		return botInfo.getToken();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (messageHandler != null) {
			messageHandler.handleMessage(this, update);
		}
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
}
