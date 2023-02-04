package com.aonufrei.energybot.service;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

	private final Logger log = LoggerFactory.getLogger(HashService.class);

	public String hashData(byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			log.error("Failed to hash data", e);
			return null;
		}
		md.update(data);
		return Hex.encodeHexString(md.digest());
	}

	public String hashText(String text) {
		if (text == null) {
			log.error("Null text was provided");
			throw new IllegalArgumentException("Cannot hash null text value");
		}
		return hashData(text.getBytes());
	}

}
