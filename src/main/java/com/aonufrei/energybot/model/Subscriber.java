package com.aonufrei.energybot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = "subscribers")
public class Subscriber {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String chatId;

	private String lastImageReceivedHash;

	private LocalDateTime creationDate = LocalDateTime.now();

	public Subscriber(String chatId) {
		this.chatId = chatId;
	}

	public Subscriber(String chatId, String lastImageReceivedHash) {
		this.chatId = chatId;
		this.lastImageReceivedHash = lastImageReceivedHash;
	}
}
