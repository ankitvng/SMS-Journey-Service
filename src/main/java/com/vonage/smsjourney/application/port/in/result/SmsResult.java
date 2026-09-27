package com.vonage.smsjourney.application.port.in.result;

import com.vonage.smsjourney.domain.model.Sms;

import java.time.LocalDateTime;

public record SmsResult(long id, String smsStatus, String recipient, String message, boolean deleted, LocalDateTime createdAt) {

    public static SmsResult from(Sms savedSms) {
        return new SmsResult(savedSms.getId(), savedSms.getStatus().name(), savedSms.getRecipient(), savedSms.getMessage(), savedSms.isDeleted(), savedSms.getCreatedAt());
    }
}
