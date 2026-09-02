package com.vonage.smsjourneyg.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsRoutingDecisionEvent {

    private Long smsId;

    private SmsJourneyDto smsJourneyDto;
}
