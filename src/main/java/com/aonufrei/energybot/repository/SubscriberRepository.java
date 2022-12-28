package com.aonufrei.energybot.repository;

import com.aonufrei.energybot.model.Subscriber;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends MongoRepository<Subscriber, String> {

	void deleteByChatId(String chatId);

	boolean existsByChatId(String chatId);

	Subscriber getSubscriptionByChatId(String chatId);

	List<Subscriber> findAllByLastImageReceivedHashIsNotIgnoreCase(String hashToIgnore);

}
