package com.vonage.smsjourneyg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsRoutingDecisionEvent {

    @NotNull
    private Long smsId;

    @NotNull
    private SmsJourneyRequest smsJourney;
}
