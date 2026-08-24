package com.vonage.smsjourneyg.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsReceivedEvent {

    private String smsId;

    private String recipient;

    private String message;

    private LocalDateTime receivedAt;
}