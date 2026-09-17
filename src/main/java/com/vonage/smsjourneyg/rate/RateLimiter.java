package com.vonage.smsjourneyg.rate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class RateLimiter {

    private final int requestsLimit;
    private final long windowMillis;

    private final AtomicReference<RateLimitState> state;

    public RateLimiter(@Value("${sms.rate-limit.requests-limit}") int requestsLimit, @Value("${sms.rate-limit.window-millis}") long windowMillis) {

        this.requestsLimit = requestsLimit;
        this.windowMillis = windowMillis;

        this.state = new AtomicReference<>(new RateLimitState(System.currentTimeMillis(), 0));
    }

    public boolean isAllowed() {

        while (true) {

            long now = System.currentTimeMillis();

            RateLimitState current = state.get();

            // Current window expired
            if (now - current.windowStart >= windowMillis) {

                RateLimitState newState = new RateLimitState(now, 1);

                if (state.compareAndSet(current, newState)) {
                    return true;
                }

                continue;
            }

            if (current.requestCount >= requestsLimit) {
                return false;
            }

            RateLimitState newState = new RateLimitState(current.windowStart, current.requestCount + 1);

            if (state.compareAndSet(current, newState)) {
                return true;
            }

        }
    }

    private static class RateLimitState {

        private final long windowStart;
        private final int requestCount;

        private RateLimitState(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
