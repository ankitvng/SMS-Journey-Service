package com.vonage.smsjourneyg.dto;

import com.vonage.smsjourneyg.enums.SmsStatus;
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

    private SmsStatus status;

    private LocalDateTime scheduledTime;
}
