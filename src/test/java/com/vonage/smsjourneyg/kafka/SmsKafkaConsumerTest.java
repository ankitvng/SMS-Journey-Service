package com.vonage.smsjourneyg.kafka;
import com.vonage.smsjourneyg.dto.SmsReceivedEvent;
import com.vonage.smsjourneyg.service.SmsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsKafkaConsumerTest {

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsKafkaConsumer consumer;

    @Test
    void shouldPassKafkaMessageToSmsService() {

        SmsReceivedEvent event =
                new SmsReceivedEvent();

        event.setSmsId("101");
        event.setRecipient(
                "+919876543210"
        );
        event.setMessage("Hello");

        consumer.consume(event);

        verify(smsService)
                .processIncomingSms(event);
    }
}
