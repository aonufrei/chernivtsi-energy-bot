package com.aonufrei.energybot.jobs;

import com.aonufrei.energybot.service.ElectricityUpdatesService;
import com.aonufrei.energybot.service.SubscribersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobExecutor {

	private final Logger log = LoggerFactory.getLogger(JobExecutor.class);

	private final ElectricityUpdatesService electricityUpdatesService;

	public JobExecutor(ElectricityUpdatesService electricityUpdatesService, SubscribersService service) {
		this.electricityUpdatesService = electricityUpdatesService;
	}

//	@Scheduled(cron = "0 0/15 * * * *")
	public void fetchImageFromWebsite() {
		log.info("Started changes detection job");
		electricityUpdatesService.detectChanges();
		log.info("Finished changes detecting job");
	}

}
