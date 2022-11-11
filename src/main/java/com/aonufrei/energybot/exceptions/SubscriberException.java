package com.aonufrei.energybot.exceptions;

public class SubscriberException extends RuntimeException {

	public SubscriberException(String message) {
		super(message);
	}

	public SubscriberException(String message, Throwable cause) {
		super(message, cause);
	}
}
