package com.vonage.smsjourneyg.kafka;

import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsJourneyKafkaProducer {
    private final KafkaTemplate<String, SmsRoutingDecisionEvent> kafkaTemplate;

    @Value("${app.kafka.sms-topic}")
    private String TOPIC;

    public void publish(SmsRoutingDecisionEvent event) {

        log.info("Publishing SMS {} to Kafka", event.getSmsId());

        kafkaTemplate.send(TOPIC, String.valueOf(event.getSmsId()), event);
    }
}
