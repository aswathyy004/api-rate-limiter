package com.aswathy.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

// 1. Add "implements RateLimiter" here
public class TokenBucket implements RateLimiter {

    private final long capacity;
    private final long refillTokensPerSecond;
    private AtomicLong availableTokens;
    private AtomicLong lastRefillTimestamp;

    public TokenBucket(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
    }

    // 2. Add @Override here to prove we are fulfilling the Interface's contract
    @Override
    public boolean tryConsume() {
        refill();
        if (availableTokens.get() > 0) {
            availableTokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTimestamp.get();
        long elapsedTime = now - lastRefill;

        if (elapsedTime > 1000) {
            long tokensToAdd = (elapsedTime / 1000) * refillTokensPerSecond;
            long newTokens = Math.min(capacity, availableTokens.get() + tokensToAdd);

            availableTokens.set(newTokens);
            lastRefillTimestamp.set(now);
        }
    }
}