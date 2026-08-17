package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.entity.Sms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsRoutingService {

    public boolean sendSms(String route, Sms sms) {

        log.info(
                "Attempting to send SMS {} through route {}",
                sms.getSmsId(),
                route
        );

        // Simulate provider

        if ("VONAGE".equals(route)) {
            return true;
        }

        return false;
    }
}
