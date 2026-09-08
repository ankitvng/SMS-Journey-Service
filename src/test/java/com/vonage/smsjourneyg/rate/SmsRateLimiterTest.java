package com.vonage.smsjourneyg.rate;

import com.vonage.smsjourneyg.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SmsRateLimiterTest {

    private SmsRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {

        RateLimitProperties properties = new RateLimitProperties();

        properties.setRequestsLimit(3);

        properties.setTimeWindow(Duration.ofMillis(200));

        rateLimiter = new SmsRateLimiter(properties);
    }

    @Test
    void shouldAllowRequestsUntilLimitIsReached() {

        String clientId = "client-1";

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));
    }

    @Test
    void shouldRejectRequestWhenLimitIsReached() {

        String clientId = "client-1";

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));

        assertFalse(rateLimiter.isAllowed(clientId));
    }

    @Test
    void shouldAllowRequestAfterOldRequestExpires() throws InterruptedException {

        String clientId = "client-1";

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));

        assertTrue(rateLimiter.isAllowed(clientId));

        assertFalse(rateLimiter.isAllowed(clientId));

        // Wait until the first requests
        // leave the sliding window.
        Thread.sleep(250);

        assertTrue(rateLimiter.isAllowed(clientId));
    }

    @Test
    void differentClientsShouldHaveIndependentLimits() {

        assertTrue(rateLimiter.isAllowed("client-1"));

        assertTrue(rateLimiter.isAllowed("client-1"));

        assertTrue(rateLimiter.isAllowed("client-1"));

        assertFalse(rateLimiter.isAllowed("client-1"));

        // Client 2 has its own bucket.
        assertTrue(rateLimiter.isAllowed("client-2"));
    }

}
