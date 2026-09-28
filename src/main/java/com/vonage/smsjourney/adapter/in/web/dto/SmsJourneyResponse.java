package com.vonage.smsjourney.adapter.in.web.dto;

import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsJourney;
import com.vonage.smsjourney.domain.model.SmsStatus;

import java.time.LocalDateTime;

public record SmsJourneyResponse(Long id,
                                 String campaignName,
                                 String routingStep,
                                 String primaryRoute,
                                 String fallbackRoute,
                                 double cost,
                                 SmsStatus status,
                                 LocalDateTime scheduledTime,
                                 Sms sms) {
    public static SmsJourneyResponse from(SmsJourney smsJourney) {
        return new SmsJourneyResponse(
                smsJourney.getId(),
                smsJourney.getCampaignName(),
                smsJourney.getRoutingStep(),
                smsJourney.getPrimaryRoute(),
                smsJourney.getFallbackRoute(),
                smsJourney.getCost(),
                smsJourney.getStatus(),
                smsJourney.getScheduledTime(),
                smsJourney.getSms()
        );
    }
}
