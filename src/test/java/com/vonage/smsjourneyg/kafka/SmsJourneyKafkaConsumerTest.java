package com.vonage.smsjourneyg.kafka;
import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.service.SmsJourneyService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsJourneyKafkaConsumerTest {

    @Mock
    private SmsJourneyService smsJourneyService;

    @InjectMocks
    private SmsJourneyKafkaConsumer consumer;

    @Test
    void shouldPassKafkaMessageToSmsService() {

        SmsRoutingDecisionEvent event =
                new SmsRoutingDecisionEvent();

        event.setSmsId(101L);
        event.setRecipient(
                "+919876543210"
        );
        event.setMessage("Hello");

        consumer.consume(event);

        verify(smsJourneyService)
                .createAndProcessJourney(event);
    }
}
