package com.vonage.smsjourney.adapter.in.web.dto;

public record SmsRequest(String recipient, String message, SmsJourneyRequest smsJourneyRequest) {
    public SmsRequest {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient must not be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be null or blank");
        }
    }
}
