package com.aonufrei.energybot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleProcessingResponse {

	private String hash;

	private byte[] image;

}
