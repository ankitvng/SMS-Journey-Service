package com.vonage.smsjourney.adapter.in.web.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;


import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@Data
public class RateLimiter {

    private final RateLimitProperties props;

    private final AtomicReference<RateLimitState> state = new AtomicReference<>(new RateLimitState(System.currentTimeMillis(), 0));


    public boolean isAllowed() {

        while (true) {

            long now = System.currentTimeMillis();

            RateLimitState current = state.get();

            // Current window expired
            if (now - current.windowStart >= props.getTimeWindow().toMillis()) {

                RateLimitState newState = new RateLimitState(now, 1);

                if (state.compareAndSet(current, newState)) {
                    return true;
                }

                continue;
            }

            if (current.requestCount >=props.getRequestsLimit()) {
                return false;
            }

            RateLimitState newState = new RateLimitState(current.windowStart, current.requestCount + 1);

            if (state.compareAndSet(current, newState)) {
                return true;
            }

        }
    }

    private record RateLimitState(long windowStart, int requestCount) {

    }
}
