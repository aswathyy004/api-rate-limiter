package com.aswathy.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket {

    private final long capacity;      // Max tokens the bucket can hold (e.g., 10)
    private final long refillRate;    // Tokens added per second (e.g., 1)

    // AtomicLong is used for thread safety (so two requests don't mess up the count)
    private final AtomicLong currentTokens;
    private final AtomicLong lastRefillTimestamp;

    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        // Start with a full bucket
        this.currentTokens = new AtomicLong(capacity);
        // Record the time we started
        this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
    }

    // The main logic: Returns true if allowed, false if blocked
    public synchronized boolean tryConsume() {
        refill(); // 1. First, add tokens based on time passed

        // 2. If we have at least 1 token, take it and return true
        if (currentTokens.get() > 0) {
            currentTokens.decrementAndGet();
            return true;
        }

        // 3. Otherwise, reject the request
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long lastRefill = lastRefillTimestamp.get();

        // Calculate how many seconds passed since last refill
        // 1 second = 1,000,000,000 nanoseconds
        long nanosElapsed = now - lastRefill;

        // Calculate tokens to add (elapsed seconds * rate)
        long tokensToAdd = (nanosElapsed / 1_000_000_000) * refillRate;

        if (tokensToAdd > 0) {
            // Update the timestamp to "now" so we don't double count next time
            lastRefillTimestamp.set(now);

            // Add tokens, but don't overflow the bucket capacity
            long newLevel = Math.min(capacity, currentTokens.get() + tokensToAdd);
            currentTokens.set(newLevel);
        }
    }
}