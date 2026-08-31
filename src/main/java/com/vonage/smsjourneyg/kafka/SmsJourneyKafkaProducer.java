package com.vonage.smsjourneyg.kafka;

import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsJourneyKafkaProducer {
    private final KafkaTemplate<String, SmsRoutingDecisionEvent> kafkaTemplate;

    private static final String TOPIC =
            "${app.kafka.sms-topic}";

    public void publish(SmsRoutingDecisionEvent event) {

        log.info(
                "Publishing SMS {} to Kafka",
                event.getSmsId()
        );

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getSmsId()),
                event
        );
    }
}
