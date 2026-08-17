package com.vonage.smsjourneyg.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void shouldCreateCacheManagerBean() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        assertNotNull(cacheManager, "CacheManager should not be null");
        assertInstanceOf(CaffeineCacheManager.class, cacheManager, "CacheManager should be an instance of CaffeineCacheManager");
    }

    @Test
    void shouldConfigureSmsCache() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        Cache smsCache = cacheManager.getCache("sms");

        assertNotNull(smsCache, "The 'sms' cache should exist");
        assertEquals("sms", smsCache.getName());
    }

    @Test
    void shouldSupportCacheOperations() {
        CacheManager cacheManager = cacheConfig.cacheManager();
        Cache smsCache = cacheManager.getCache("sms");

        assertNotNull(smsCache);

        // Test basic put/get operations on the configured cache
        smsCache.put(1L, "cached-data");
        assertEquals("cached-data", smsCache.get(1L, String.class));

        smsCache.evict(1L);
        assertNull(smsCache.get(1L));
    }
}