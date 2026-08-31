package com.vonage.smsjourneyg.kafka;

import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.mapper.SmsMapper;
import com.vonage.smsjourneyg.service.SmsJourneyService;
import com.vonage.smsjourneyg.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsJourneyKafkaConsumer {

    private final SmsJourneyService smsJourneyService;

    @KafkaListener(
            topics = "${app.kafka.sms-topic}",
            groupId = "${app.kafka.group-id}"
    )
    public void consume(SmsRoutingDecisionEvent event) {

        log.info(
                "Received SMS {} for journey processing",
                event.getSmsId()
        );

        smsJourneyService.createAndProcessJourney(
                event
        );
    }

}
