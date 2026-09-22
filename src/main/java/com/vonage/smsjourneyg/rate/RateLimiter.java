package com.vonage.smsjourneyg.rate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@Data
public class RateLimiter {

    @Value("${sms.rate-limit.requests-limit}")
    private final int requestsLimit;

    @Value("${sms.rate-limit.window-millis}")
    private final Duration windowMillis;

    private final AtomicReference<RateLimitState> state = new AtomicReference<>(new RateLimitState(System.currentTimeMillis(), 0));


    public boolean isAllowed() {

        while (true) {

            long now = System.currentTimeMillis();

            RateLimitState current = state.get();

            // Current window expired
            if (now - current.windowStart >= windowMillis.toMillis()) {

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

    @Data
    private static class RateLimitState {

        private final long windowStart;
        private final int requestCount;
    }
}
