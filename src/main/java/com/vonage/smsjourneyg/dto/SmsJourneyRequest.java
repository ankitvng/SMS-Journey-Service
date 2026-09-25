package com.vonage.smsjourneyg.dto;

import lombok.Data;

@Data
public class SmsJourneyRequest {
    private String campaignName;

    private String routingStep;

    private String primaryRoute;

    private String fallbackRoute;

    private double cost;

}
