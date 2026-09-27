package com.vonage.smsjourney.adapter.in.kafka;

import com.vonage.smsjourney.adapter.in.kafka.SmsJourneyKafkaConsumer;
import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;
import com.vonage.smsjourney.application.port.in.CreateJourneyFromEventUseCase;
import com.vonage.smsjourney.domain.model.SmsJourney;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SmsJourneyKafkaConsumerTest {

    @Mock
    private CreateJourneyFromEventUseCase useCase;

    private SmsJourneyKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SmsJourneyKafkaConsumer(useCase);
    }

    @Test
    void shouldPassKafkaMessageToSmsService() {

        SmsRoutingDecisionEvent event = new SmsRoutingDecisionEvent(101L, new SmsJourney());
        consumer.consume(event);

        then(useCase).should().createJourneyFromEvent(event);
    }
}
