package com.vonage.smsjourneyg.rate;

import com.vonage.smsjourneyg.config.RateLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class SmsRateLimiter {

    private final int requestsLimit;

    private final long timeWindowMillis;

    private final ConcurrentHashMap<String, RateLimitBucket> clients = new ConcurrentHashMap<>();

    public SmsRateLimiter(RateLimitProperties properties) {

        if (properties.getRequestsLimit() <= 0) {
            throw new IllegalArgumentException("requestsLimit must be greater than zero");
        }

        if (properties.getTimeWindow() == null || properties.getTimeWindow().isZero() || properties.getTimeWindow().isNegative()) {

            throw new IllegalArgumentException("timeWindow must be greater than zero");
        }

        this.requestsLimit = properties.getRequestsLimit();

        this.timeWindowMillis = properties.getTimeWindow().toMillis();

        log.info("SMS rate limiter initialized: limit={}, window={}ms", requestsLimit, timeWindowMillis);
    }

    public boolean isAllowed(String clientId) {

        RateLimitBucket bucket = clients.computeIfAbsent(clientId, key -> new RateLimitBucket());

        synchronized (bucket) {

            long currentTime = System.currentTimeMillis();

            ConcurrentLinkedDeque<Long> timestamps = bucket.getTimestamps();

            AtomicInteger requestCount = bucket.getRequestCount();

            removeExpiredRequests(timestamps, requestCount, currentTime);

            if (requestCount.get() >= requestsLimit) {

                log.warn("Rate limit exceeded for client {}: {}/{}", clientId, requestCount.get(), requestsLimit);

                return false;
            }

            timestamps.addLast(currentTime);

            requestCount.incrementAndGet();

            log.debug("Request allowed for client {}: {}/{}", clientId, requestCount.get(), requestsLimit);

            return true;
        }
    }

    private void removeExpiredRequests(ConcurrentLinkedDeque<Long> timestamps, AtomicInteger requestCount, long currentTime) {

        long expiryTime = currentTime - timeWindowMillis;

        while (true) {

            Long timestamp = timestamps.peekFirst();

            if (timestamp == null) {
                break;
            }

            if (timestamp > expiryTime) {
                break;
            }

            timestamps.pollFirst();

            requestCount.decrementAndGet();
        }
    }

    public void clear() {

        clients.clear();
    }

    public int getClientRequestCount(String clientId) {

        RateLimitBucket bucket = clients.get(clientId);

        if (bucket == null) {
            return 0;
        }

        synchronized (bucket) {

            long currentTime = System.currentTimeMillis();

            removeExpiredRequests(bucket.getTimestamps(), bucket.getRequestCount(), currentTime);

            return bucket.getRequestCount().get();
        }
    }
}
