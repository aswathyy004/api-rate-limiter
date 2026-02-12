package com.aswathy.ratelimiter.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FixedWindowCounter implements RateLimiter {

    private final int limit;
    private final long windowSizeInMillis;
    private final AtomicLong windowStart;
    private final AtomicInteger counter;

    public FixedWindowCounter(int limit, long windowSizeInMillis) {
        this.limit = limit;
        this.windowSizeInMillis = windowSizeInMillis;

        // Track when the current time window started
        this.windowStart = new AtomicLong(System.currentTimeMillis());
        // Track how many requests happened in this window
        this.counter = new AtomicInteger(0);
    }

    @Override
    public boolean tryConsume() {
        long now = System.currentTimeMillis();
        // Math trick: round down the current time to the nearest window boundary
        long currentWindow = (now / windowSizeInMillis) * windowSizeInMillis;

        // If time has passed into a NEW window, we must reset the counter
        if (windowStart.get() != currentWindow) {
            // synchronized ensures multiple threads don't reset it at the exact same millisecond
            synchronized (this) {
                if (windowStart.get() != currentWindow) {
                    windowStart.set(currentWindow);
                    counter.set(0); // Reset tokens back to 0!
                }
            }
        }

        // Increment the counter. If it's less than or equal to the limit, allow!
        return counter.incrementAndGet() <= limit;
    }
}