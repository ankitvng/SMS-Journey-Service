package com.vonage.smsjourneyg.rate;


import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SmsRateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {

        SmsRateLimiter rateLimiter = new SmsRateLimiter(5, 1000);

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed());
        }
    }


    @Test
    void shouldRejectRequestsAfterLimitIsReached() {

        SmsRateLimiter rateLimiter = new SmsRateLimiter(5, 1000);

        // First 5 requests should pass
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed());
        }

        // 6th request should be rejected
        assertFalse(rateLimiter.isAllowed());

        // Additional requests should also be rejected
        assertFalse(rateLimiter.isAllowed());
        assertFalse(rateLimiter.isAllowed());
    }


    @Test
    void shouldResetAfterTimeWindowExpires() throws InterruptedException {

        SmsRateLimiter rateLimiter = new SmsRateLimiter(2, 100);

        // Window 1
        assertTrue(rateLimiter.isAllowed());
        assertTrue(rateLimiter.isAllowed());

        // Limit reached
        assertFalse(rateLimiter.isAllowed());

        // Wait for window to expire
        Thread.sleep(150);

        // New window
        assertTrue(rateLimiter.isAllowed());
        assertTrue(rateLimiter.isAllowed());

        // Limit reached again
        assertFalse(rateLimiter.isAllowed());
    }


    @Test
    void shouldHandleConcurrentRequests() throws InterruptedException {

        int requestLimit = 100;
        int totalRequests = 1000;

        SmsRateLimiter rateLimiter = new SmsRateLimiter(requestLimit, 5000);

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

        assertEquals(requestLimit, allowedRequests.get());

        assertEquals(totalRequests - requestLimit, rejectedRequests.get());
    }
}
