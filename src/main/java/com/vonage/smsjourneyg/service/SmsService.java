package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.cache.SmsCache;
import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.exception.SmsNotFoundException;
import com.vonage.smsjourneyg.kafka.SmsJourneyKafkaProducer;
import com.vonage.smsjourneyg.mapper.SmsMapper;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import com.vonage.smsjourneyg.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsRepository smsRepository;
    private final SmsCache smsCache;
    private final SmsMapper smsMapper;
    private final SmsJourneyKafkaProducer producer;
    private final SmsJourneyRepository journeyRepository;


    public List<SmsResponseDto> getAllSms() {
        log.info("Getting all SMS");
        return smsCache.getAllSms();
    }

    public Page<SmsResponseDto> getAllSms(Pageable pageable) {
        log.info("Getting SMS page {} of size {}", pageable.getPageNumber(), pageable.getPageSize());

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

        Sms sms = smsRepository.findBySmsIdAndDeletedIsFalse(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));

        return smsMapper.toResponseDto(sms);
    }

    public SmsResponseDto createSms(SmsRequestDto request) {
        log.info("Creating SMS for {}", request.getRecipient());

        Sms sms = smsMapper.toEntity(request);
        sms.setStatus(SmsStatus.CREATED);
        sms.setCreatedAt(LocalDateTime.now());

        Sms savedSms = smsRepository.save(sms);
        smsCache.invalidate();

        log.info("SMS {} created successfully", savedSms.getSmsId());

        SmsRoutingDecisionEvent event = new SmsRoutingDecisionEvent();
        event.setSmsId(savedSms.getSmsId());
        event.setSmsJourney(request.getSmsJourneyRequest());

        producer.publish(event);

        return smsMapper.toResponseDto(savedSms);
    }

    @Transactional
    public void deleteSms(Long smsId) {
        log.info("Soft deleting SMS {}", smsId);

        Sms sms = smsRepository.findBySmsIdAndDeletedIsFalse(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));

        sms.setDeleted(true);
        smsRepository.save(sms);
        journeyRepository.deleteBySmsSmsId(smsId);
        smsCache.invalidate();

        log.info("SMS {} soft deleted successfully", smsId);
    }

}
