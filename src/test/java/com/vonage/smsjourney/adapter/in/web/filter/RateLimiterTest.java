package com.vonage.smsjourney.adapter.in.web.filter;


import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {

        RateLimiter rateLimiter = rateLimiter(5, Duration.ofSeconds(1));

        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.isAllowed()).isTrue();
        }
    }


    @Test
    void shouldRejectRequestsAfterLimitIsReached() {

        RateLimiter rateLimiter = rateLimiter(5, Duration.ofSeconds(1));

        // First 5 requests should pass
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.isAllowed()).isTrue();
        }

        // 6th request should be rejected
        assertThat(rateLimiter.isAllowed()).isFalse();

        // Additional requests should also be rejected
        assertThat(rateLimiter.isAllowed()).isFalse();
        assertThat(rateLimiter.isAllowed()).isFalse();
    }


    @Test
    void shouldResetAfterTimeWindowExpires() throws InterruptedException {

        RateLimiter rateLimiter = rateLimiter(2, Duration.ofMillis(100));

        // Window 1
        assertThat(rateLimiter.isAllowed()).isTrue();
        assertThat(rateLimiter.isAllowed()).isTrue();

        // Limit reached
        assertThat(rateLimiter.isAllowed()).isFalse();

        // Wait for window to expire
        Thread.sleep(150);

        // New window
        assertThat(rateLimiter.isAllowed()).isTrue();
        assertThat(rateLimiter.isAllowed()).isTrue();

        // Limit reached again
        assertThat(rateLimiter.isAllowed()).isFalse();
    }


    @Test
    void shouldHandleConcurrentRequests() throws InterruptedException {

        int requestLimit = 100;
        int totalRequests = 1000;

        RateLimiter rateLimiter = rateLimiter(requestLimit, Duration.ofSeconds(5));

        ExecutorService executor = Executors.newFixedThreadPool(20);

        CountDownLatch startLatch = new CountDownLatch(1);

        CountDownLatch finishLatch = new CountDownLatch(totalRequests);

        AtomicInteger allowedRequests = new AtomicInteger(0);

        AtomicInteger rejectedRequests = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {

            executor.submit(() -> {

                try {

                    // Make all threads start together
                    startLatch.await();

                    if (rateLimiter.isAllowed()) {
                        allowedRequests.incrementAndGet();
                    } else {
                        rejectedRequests.incrementAndGet();
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    finishLatch.countDown();
                }
            });
        }
        // Start all threads
        startLatch.countDown();
        // Wait for all requests to finish
        finishLatch.await();

        executor.shutdown();

        assertThat(allowedRequests.get()).isEqualTo(requestLimit);

        assertThat(rejectedRequests.get()).isEqualTo(totalRequests - requestLimit);
    }

    private RateLimiter rateLimiter(int requestsLimit, Duration timeWindow) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setRequestsLimit(requestsLimit);
        properties.setTimeWindow(timeWindow);
        return new RateLimiter(properties);
    }
}
