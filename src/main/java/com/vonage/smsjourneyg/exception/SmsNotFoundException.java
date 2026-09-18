package com.vonage.smsjourneyg.exception;

public class SmsNotFoundException extends RuntimeException {

    public SmsNotFoundException(Long smsId) {
        super("SMS not found: " + smsId);
    }
}