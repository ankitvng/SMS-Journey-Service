package com.vonage.smsjourney.domain.exception;

public class JourneyNotFoundException extends RuntimeException {
    public JourneyNotFoundException(Long journeyId) {
        super("Journey not found: " + journeyId);
    }
}
