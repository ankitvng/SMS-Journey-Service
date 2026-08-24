package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.config.SmsCache;
import com.vonage.smsjourneyg.dto.SmsReceivedEvent;
import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.exception.SmsNotFoundException;
import com.vonage.smsjourneyg.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsRepository smsRepository;
    private final SmsCache smsCache;

    public List<SmsResponseDto> getAllSms() {
        log.info("Getting all SMS");
        return smsCache.getAllSms();
    }

    public Page<SmsResponseDto> getAllSms(Pageable pageable) {
        log.info(
                "Getting SMS page {} of size {}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        List<SmsResponseDto> allSms = smsCache.getAllSms();

        int start = (int) pageable.getOffset();

        if (start >= allSms.size()) {
            return new PageImpl<>(List.of(), pageable, allSms.size());
        }

        int end = Math.min(start + pageable.getPageSize(), allSms.size());
        List<SmsResponseDto> pageContent = allSms.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allSms.size());
    }

    public SmsResponseDto getSms(Long smsId) {
        log.info("Fetching SMS {}", smsId);

        Sms sms = smsRepository.findBySmsIdAndDeletedIsFalse(smsId)
                .orElseThrow(() -> new SmsNotFoundException(smsId));

        return convertToDto(sms);
    }

    public SmsResponseDto createSms(SmsRequestDto request) {
        log.info("Creating SMS for {}", request.getRecipient());

        Sms sms = new Sms();
        sms.setRecipient(request.getRecipient());
        sms.setMessage(request.getMessage());
        sms.setStatus(SmsStatus.CREATED);
        sms.setCreatedAt(LocalDateTime.now());

        Sms savedSms = smsRepository.save(sms);
        log.info("SMS {} created successfully", savedSms.getSmsId());

        smsCache.invalidate();

        return convertToDto(savedSms);
    }

    public void deleteSms(Long smsId) {
        log.info("Soft deleting SMS {}", smsId);

        Sms sms = smsRepository.findBySmsIdAndDeletedIsFalse(smsId)
                .orElseThrow(() -> new SmsNotFoundException(smsId));

        sms.setDeleted(true);
        smsRepository.save(sms);

        log.info("SMS {} soft deleted successfully", smsId);

        smsCache.invalidate();
    }

    public void processIncomingSms(
            SmsReceivedEvent event) {

        Optional<Sms> existingSms =
                smsRepository
                        .findByExternalSmsId(
                                event.getSmsId()
                        );

        if (existingSms.isPresent()) {

            log.info(
                    "SMS {} already exists. Ignoring duplicate event.",
                    event.getSmsId()
            );

            return;
        }

        Sms sms = new Sms();

        sms.setExternalSmsId(
                String.valueOf(event.getSmsId())
        );

        sms.setRecipient(
                event.getRecipient()
        );

        sms.setMessage(
                event.getMessage()
        );

        sms.setStatus(SmsStatus.SCHEDULED);

        sms.setCreatedAt(
                event.getReceivedAt()
        );

        smsRepository.save(sms);

        smsCache.invalidate();
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