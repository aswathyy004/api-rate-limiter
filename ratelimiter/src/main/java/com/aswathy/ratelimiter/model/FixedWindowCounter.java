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
        this.windowStart = new AtomicLong(System.currentTimeMillis());
        this.counter = new AtomicInteger(0);
    }

    @Override
    public RateLimitResult tryConsume() {
        long now = System.currentTimeMillis();
        long currentWindow = (now / windowSizeInMillis) * windowSizeInMillis;

        // Check if we moved to a new time window
        if (windowStart.get() != currentWindow) {
            synchronized (this) {
                if (windowStart.get() != currentWindow) {
                    windowStart.set(currentWindow);
                    counter.set(0);
                }
            }
        }

        int currentCount = counter.incrementAndGet();

        // Calculate remaining tokens (prevent negative numbers)
        long remaining = Math.max(0, limit - currentCount);

        if (currentCount <= limit) {
            // Allowed! Return true + remaining count
            return new RateLimitResult(true, remaining);
        } else {
            // Blocked! Return false + 0 remaining
            return new RateLimitResult(false, 0);
        }
    }
}