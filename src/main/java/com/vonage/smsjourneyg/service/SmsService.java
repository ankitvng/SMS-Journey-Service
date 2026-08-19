package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsRepository smsRepository;

    @CachePut(value = "sms", key = "#result.smsId")
    public SmsResponseDto createSms(SmsRequestDto request) {

        log.info(
                "Creating SMS for recipient {}",
                request.getRecipient()
        );

        Sms sms = new Sms();

        sms.setRecipient(request.getRecipient());
        sms.setMessage(request.getMessage());
        sms.setStatus(SmsStatus.CREATED);
        sms.setCreatedAt(LocalDateTime.now());

        Sms savedSms = smsRepository.save(sms);

        log.info(
                "SMS {} created successfully",
                savedSms.getSmsId()
        );

        return convertToDto(savedSms);
    }

    @Cacheable(value = "sms", key = "#smsId")
    public SmsResponseDto getSms(Long smsId) {

        log.info("Fetching SMS {}", smsId);

        Sms sms = smsRepository.findById(smsId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "SMS not found: " + smsId
                        )
                );

        return convertToDto(sms);
    }

    public List<SmsResponseDto> getAllSms() {

        log.info("Fetching all SMS");

        return smsRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @CacheEvict(value = "sms", key = "#smsId")
    public void deleteSms(Long smsId) {

        log.info("Deleting SMS {}", smsId);

        if (!smsRepository.existsById(smsId)) {
            throw new RuntimeException(
                    "SMS not found: " + smsId
            );
        }

        smsRepository.deleteById(smsId);

        log.info(
                "SMS {} deleted successfully",
                smsId
        );
    }

    private SmsResponseDto convertToDto(Sms sms) {

        SmsResponseDto dto = new SmsResponseDto();

        dto.setSmsId(sms.getSmsId());
        dto.setRecipient(sms.getRecipient());
        dto.setMessage(sms.getMessage());
        dto.setStatus(sms.getStatus());
        dto.setCreatedAt(sms.getCreatedAt());

        return dto;
    }
}