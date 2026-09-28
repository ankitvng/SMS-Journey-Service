package com.vonage.smsjourney.adapter.in.kafka;

import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;
import com.vonage.smsjourney.application.port.in.CreateJourneyFromEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsJourneyKafkaConsumer {

    private final CreateJourneyFromEventUseCase useCase;

    @KafkaListener(topics = "${app.kafka.sms-topic}", groupId = "${app.kafka.sms-group-id}")
    public void consume(SmsRoutingDecisionEvent event) {
        log.info("Kafka message received: smsId={}", event.smsId());
        useCase.createJourneyFromEvent(event);
    }
}
