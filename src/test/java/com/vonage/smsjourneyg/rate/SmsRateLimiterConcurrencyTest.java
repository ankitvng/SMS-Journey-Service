package com.vonage.smsjourneyg.rate;

import com.vonage.smsjourneyg.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmsRateLimiterConcurrencyTest {

    private SmsRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {

        RateLimitProperties properties = new RateLimitProperties();

        properties.setRequestsLimit(10);

        properties.setTimeWindow(Duration.ofSeconds(10));

        rateLimiter = new SmsRateLimiter(properties);
    }

    @Test
    void shouldAllowOnlyConfiguredNumberOfConcurrentRequests() throws InterruptedException {

        String clientId = "same-client";

        int numberOfThreads = 50;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch startLatch = new CountDownLatch(1);

        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {

            futures.add(executor.submit(() -> {

                try {

                    // Make all threads start
                    // at approximately the same time.
                    startLatch.await();

                    return rateLimiter.isAllowed(clientId);

                } finally {

                    doneLatch.countDown();
                }
            }));
        }

        // Release all threads.
        startLatch.countDown();

        // Wait for all threads.
        doneLatch.await();

        int allowedRequests = 0;

        for (Future<Boolean> future : futures) {

            try {
                if (future.get()) {
                    allowedRequests++;
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();

        assertEquals(10, allowedRequests);
    }
}
