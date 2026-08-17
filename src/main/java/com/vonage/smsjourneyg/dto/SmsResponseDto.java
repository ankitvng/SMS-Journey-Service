package com.vonage.smsjourneyg.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SmsResponseDto {

    private Long smsId;

    private String recipient;

    private String message;

    private String status;

    private LocalDateTime createdAt;

    private List<SmsJourneyDto> journeys;
}