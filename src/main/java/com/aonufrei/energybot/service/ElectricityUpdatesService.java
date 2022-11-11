package com.aonufrei.energybot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElectricityUpdatesService {

	private final Logger log = LoggerFactory.getLogger(ElectricityUpdatesService.class);

	private final ImageProcessingService imageProcessingService;

	private final MailingService mailingService;

	public ElectricityUpdatesService(ImageProcessingService imageProcessingService, MailingService mailingService) {
		this.imageProcessingService = imageProcessingService;
		this.mailingService = mailingService;
	}


	public void detectChanges() {
		log.info("Get image bytes");
		byte[] imageBytes = imageProcessingService.getImageBytes();
		if (imageBytes == null) {
			return;
		}
		log.info("Notifying subscribers");
		mailingService.notifySubscribers(imageBytes);
	}


}
