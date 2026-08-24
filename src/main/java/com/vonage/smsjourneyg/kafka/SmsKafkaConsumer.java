package com.vonage.smsjourneyg.kafka;

import com.vonage.smsjourneyg.dto.SmsReceivedEvent;
import com.vonage.smsjourneyg.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsKafkaConsumer {

    private final SmsService smsService;

    @KafkaListener(
            topics = "sms-received",
            groupId = "sms-journey-service"
    )
    public void consume(SmsReceivedEvent event) {

        log.info(
                "Received SMS event {}",
                event.getSmsId()
        );

        smsService.processIncomingSms(event);
    }
}
