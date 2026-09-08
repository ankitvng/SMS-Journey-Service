package com.vonage.smsjourneyg.rate;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitBucket {

    private final AtomicInteger requestCount =
            new AtomicInteger(0);

    private final ConcurrentLinkedDeque<Long> timestamps =
            new ConcurrentLinkedDeque<>();

    public AtomicInteger getRequestCount() {
        return requestCount;
    }

    public ConcurrentLinkedDeque<Long> getTimestamps() {
        return timestamps;
    }
}
