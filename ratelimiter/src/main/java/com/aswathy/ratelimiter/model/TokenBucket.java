package com.aswathy.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket implements RateLimiter {

    private final long capacity;
    private final long refillTokensPerSecond;
    private final AtomicLong availableTokens;
    private final AtomicLong lastRefillTimestamp;

    public TokenBucket(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public RateLimitResult tryConsume() {
        refill();

        long currentTokens = availableTokens.get();

        if (currentTokens > 0) {
            availableTokens.decrementAndGet();
            // Allowed! We return true, and the remaining count (current - 1)
            return new RateLimitResult(true, currentTokens - 1);
        }

        // Blocked! We return false, and 0 remaining
        return new RateLimitResult(false, 0);
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTimestamp.get();
        long elapsedTime = now - lastRefill;

        // Only refill if at least 1 second has passed (simplification for this example)
        if (elapsedTime > 1000) {
            long tokensToAdd = (elapsedTime / 1000) * refillTokensPerSecond;
            if (tokensToAdd > 0) {
                long newTokens = Math.min(capacity, availableTokens.get() + tokensToAdd);
                availableTokens.set(newTokens);
                lastRefillTimestamp.set(now);
            }
        }
    }
}