package com.vonage.smsjourneyg.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.repository.SmsRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class SmsCache {

    private final LoadingCache<String, List<SmsResponseDto>> cache;

    public SmsCache(SmsRepository smsRepository,
                        @Value("${sms-journey.cache.expire-after-write}")
                        Duration expireAfterWrite) {
        this.cache = Caffeine
                .newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(expireAfterWrite)
                .build(key -> {
                    log.info("Cache miss. Fetching SMS from database");
                    List<Sms> smsList = smsRepository.findAllByDeletedIsFalse();
                    return smsList.stream()
                            .map(this::convertToDto)
                            .toList();
                });
    }

    @PostConstruct
    public void warmUpCache() {
        log.info("Warming up SMS cache on startup");
        cache.get("all");
        log.info("SMS cache warm-up complete");
    }

    public List<SmsResponseDto> getAllSms() {
        return cache.get("all");
    }

    public void invalidate() {
        log.info("Invalidating SMS cache");
        cache.invalidate("all");
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