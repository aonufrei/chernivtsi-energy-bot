package com.aonufrei.energybot.repository;

import com.aonufrei.energybot.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

	void deleteByChatId(String chatId);

	boolean existsByChatId(String chatId);

	Subscriber getSubscriptionByChatId(String chatId);

	List<Subscriber> findAllByLastImageReceivedHashIsNotIgnoreCase(String hashToIgnore);

}
