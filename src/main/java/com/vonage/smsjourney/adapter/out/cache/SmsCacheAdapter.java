package com.vonage.smsjourney.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vonage.smsjourney.application.port.out.cache.SmsCachePort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.domain.model.Sms;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class SmsCacheAdapter implements SmsCachePort {


    private final LoadingCache<String, List<Sms>> cache;

    public SmsCacheAdapter(SmsRepositoryPort smsRepository, @Value("${sms-journey.cache.expire-after-write}") Duration expireAfterWrite) {
        this.cache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(expireAfterWrite).build(key -> {
            log.info("Cache miss — fetching SMS from database");
            return smsRepository.findAllActive().stream().toList();
        });
    }

    @PostConstruct
    public void warmUp() {
        log.info("Warming up SMS cache on startup");
        cache.get("all");
        log.info("SMS cache warm-up complete");
    }



    @Override
    public void invalidateAllSms() {
        log.info("Invalidating SMS cache");
        cache.invalidate("all");
    }

    @Override
    public List<Sms> getSmsBySmsId(Long smsId) {
        return cache.get("smsId");
    }


    @Override
    public List<Sms> getAllSms() {
        return cache.get("all");
    }

}
