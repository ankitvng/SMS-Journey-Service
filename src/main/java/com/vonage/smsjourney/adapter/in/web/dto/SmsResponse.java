package com.vonage.smsjourney.adapter.in.web.dto;

import com.vonage.smsjourney.domain.model.Sms;

import java.time.LocalDateTime;

public record SmsResponse(long id, String smsStatus, String recipient, String message, boolean deleted, LocalDateTime createdAt) {
    public static SmsResponse from(Sms savedSms) {
        return new SmsResponse(
                savedSms.getId(),
                savedSms.getStatus().name(),
                savedSms.getRecipient(),
                savedSms.getMessage(),
                savedSms.isDeleted(),
                savedSms.getCreatedAt()
        );
    }
}
