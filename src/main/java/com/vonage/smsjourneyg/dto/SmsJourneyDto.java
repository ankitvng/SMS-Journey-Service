package com.vonage.smsjourneyg.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsJourneyDto {

    private Long id;

    private String campaignName;

    private String routingStep;

    private String primaryRoute;

    private String fallbackRoute;

    private double cost;

    private String status;

    private LocalDateTime scheduledTime;
}
