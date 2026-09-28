package com.vonage.smsjourney.adapter.out.messaging;

import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;
import com.vonage.smsjourney.application.port.out.messaging.SmsEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSmsEventPublisherAdapter implements SmsEventPublisherPort {

    private final KafkaTemplate<String, SmsRoutingDecisionEvent> kafkaTemplate;
    private final String topic;

    public KafkaSmsEventPublisherAdapter(KafkaTemplate<String, SmsRoutingDecisionEvent> kafkaTemplate, @Value("${app.kafka.sms-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(SmsRoutingDecisionEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.smsId()), event);
    }

}
