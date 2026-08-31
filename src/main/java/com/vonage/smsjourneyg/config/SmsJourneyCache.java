package com.vonage.smsjourneyg.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class SmsJourneyCache {

    private final LoadingCache<Long, List<SmsJourneyDto>> cache;

    public SmsJourneyCache(
            SmsJourneyRepository smsJourneyRepository,
            @Value("${sms-journey.cache.expire-after-write}")
            Duration expireAfterWrite) {

        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(expireAfterWrite)
                .build(smsId -> {

                    log.info(
                            "SmsJourney cache miss for smsId {}. Fetching from database.",
                            smsId
                    );

                    return smsJourneyRepository
                            .findBySmsSmsId(smsId)
                            .stream()
                            .map(this::convertToDto)
                            .toList();
                });
    }
    public List<SmsJourneyDto> getJourneysBySmsId(
            Long smsId) {

        log.debug(
                "Fetching SmsJourneys for smsId {}",
                smsId
        );

        return cache.get(smsId);
    }

    public void invalidate(Long smsId) {

        log.info(
                "Invalidating SmsJourney cache for smsId {}",
                smsId
        );

        cache.invalidate(smsId);
    }

    public void invalidateAll() {

        log.info("Invalidating complete SmsJourney cache");

        cache.invalidateAll();
    }

    private SmsJourneyDto convertToDto(
            SmsJourney journey) {

        SmsJourneyDto dto = new SmsJourneyDto();

        dto.setId(journey.getId());
        dto.setCampaignName(journey.getCampaignName());
        dto.setRoutingStep(journey.getRoutingStep());
        dto.setPrimaryRoute(journey.getPrimaryRoute());
        dto.setFallbackRoute(journey.getFallbackRoute());
        dto.setCost(journey.getCost());
        dto.setStatus(journey.getStatus());
        dto.setScheduledTime(journey.getScheduledTime());

        return dto;
    }
}

