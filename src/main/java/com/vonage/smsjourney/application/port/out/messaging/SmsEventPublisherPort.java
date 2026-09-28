package com.vonage.smsjourney.application.port.out.messaging;

import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;

public interface SmsEventPublisherPort {
    void publish(SmsRoutingDecisionEvent event);
}
