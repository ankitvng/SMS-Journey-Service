package com.vonage.smsjourney.application.port.in;

import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;

public interface CreateJourneyFromEventUseCase {
    void createJourneyFromEvent(SmsRoutingDecisionEvent event);
}
