package com.aonufrei.energybot.service;

import com.aonufrei.energybot.exceptions.SubscriberException;
import com.aonufrei.energybot.model.Subscriber;
import com.aonufrei.energybot.repository.SubscriberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscribersService {

	private final SubscriberRepository repository;

	public SubscribersService(SubscriberRepository repository) {
		this.repository = repository;
	}

	public boolean isChatSubscribed(String chatId) {
		return repository.existsByChatId(chatId);
	}

	public void subscribeChat(String chatId) {
		if (repository.existsByChatId(chatId)) {
			throw new SubscriberException("User is already registered");
		}
		repository.save(new Subscriber(chatId));
	}

	public void unsubscribeChat(String chatId) {
		repository.deleteByChatId(chatId);
	}

	public void save(Subscriber subscribe) {
		subscribe.setId(UUID.randomUUID().toString());
		subscribe.setCreationDate(LocalDateTime.now());
		repository.save(subscribe);
	}

	public void saveAll(List<Subscriber> subscribers) {
		LocalDateTime now = LocalDateTime.now();
		subscribers.forEach(s -> s.setCreationDate(now));
		repository.saveAll(subscribers);
	}

	public Subscriber getSubscriptionByChatId(String chatId) {
		return repository.getSubscriptionByChatId(chatId);
	}

	public List<Subscriber> getSubscribersThatDidntReceive(String currentHash) {
		return repository.findAllByLastImageReceivedHashIsNotIgnoreCase(currentHash);
	}
}
