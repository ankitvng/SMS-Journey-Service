package com.vonage.smsjourney.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vonage.smsjourney.application.port.out.cache.SmsJourneyCachePort;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.domain.model.SmsJourney;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class SmsJourneyCacheAdapter implements SmsJourneyCachePort {

    private final LoadingCache<Long, List<SmsJourney>> cache;

    public SmsJourneyCacheAdapter(SmsJourneyRepositoryPort smsJourneyRepository, @Value("${sms-journey.cache.expire-after-write}") Duration expireAfterWrite) {
        this.cache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(expireAfterWrite).build(smsId -> {
            log.info("SmsJourney cache miss for smsId={}. Fetching from database.", smsId);
            return smsJourneyRepository.findBySmsId(smsId);
        });
    }

    @Override
    public List<SmsJourney> getJourneysBySmsId(Long smsId) {
        log.debug("Fetching SmsJourneys for smsId={}", smsId);
        return cache.get(smsId);
    }

    @Override
    public void invalidate(Long smsId) {
        log.info("Invalidating SmsJourney cache for smsId={}", smsId);
        cache.invalidate(smsId);
    }

    @Override
    public void invalidateAll() {
        log.info("Invalidating complete SmsJourney cache");
        cache.invalidateAll();
    }
}
