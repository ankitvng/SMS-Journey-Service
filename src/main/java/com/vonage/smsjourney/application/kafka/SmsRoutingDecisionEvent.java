package com.vonage.smsjourney.application.kafka;


import com.vonage.smsjourney.domain.model.SmsJourney;


public record SmsRoutingDecisionEvent(Long smsId, SmsJourney smsJourney) {
}
