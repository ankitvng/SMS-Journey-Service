package com.vonage.smsjourneyg.mapper;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import org.springframework.stereotype.Component;

@Component
public class SmsMapper {

    public Sms toEntity(SmsRequestDto request) {
        if (request == null) return null;

        Sms sms = new Sms();
        sms.setRecipient(request.getRecipient());
        sms.setMessage(request.getMessage());
        return sms;
    }
    public SmsResponseDto toResponseDto(Sms sms) {
        if (sms == null) return null;

        SmsResponseDto dto = new SmsResponseDto();
        dto.setSmsId(sms.getSmsId());
        dto.setRecipient(sms.getRecipient());
        dto.setMessage(sms.getMessage());
        dto.setStatus(sms.getStatus());
        dto.setCreatedAt(sms.getCreatedAt());
        return dto;
    }

    public Sms toEntity(SmsRoutingDecisionEvent event) {
        if (event == null) return null;
        Sms sms = new Sms();

        return sms;
    }
}
