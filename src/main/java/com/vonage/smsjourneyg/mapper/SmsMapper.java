package com.vonage.smsjourneyg.mapper;

import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import org.springframework.stereotype.Component;

@Component
public class SmsMapper {

    public Sms toEntity(SmsRoutingDecisionEvent event) {
        if (event == null) return null;
        Sms sms = new Sms();
        sms.setStatus(SmsStatus.SCHEDULED);

        return sms;
    }
}
