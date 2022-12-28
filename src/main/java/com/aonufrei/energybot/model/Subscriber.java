package com.aonufrei.energybot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Document(collection = "subscribers")
public class Subscriber {

	@Id
	private String id = UUID.randomUUID().toString();

	private String chatId;

	private String lastImageReceivedHash;

	@CreatedDate
	private LocalDateTime creationDate = LocalDateTime.now();

	public Subscriber(String chatId) {
		this.chatId = chatId;
	}

	public Subscriber(String chatId, String lastImageReceivedHash) {
		this.chatId = chatId;
		this.lastImageReceivedHash = lastImageReceivedHash;
	}
}
