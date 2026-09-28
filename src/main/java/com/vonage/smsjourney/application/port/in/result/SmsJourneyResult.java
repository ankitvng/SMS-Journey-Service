package com.vonage.smsjourney.application.port.in.result;

import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;

import java.time.LocalDateTime;

public record SmsJourneyResult(Long id, String campaignName, String routingStep, String primaryRoute, String fallbackRoute, double cost,
                               SmsStatus status, LocalDateTime scheduledTime, Sms sms) {
}
