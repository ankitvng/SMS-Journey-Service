package com.vonage.smsjourneyg.dto;

import com.vonage.smsjourneyg.enums.SmsStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SmsResponseDto {

    private Long smsId;

    private String recipient;

    private String message;

    private SmsStatus status;

    private LocalDateTime createdAt;

    private List<SmsJourneyDto> journeys;
}