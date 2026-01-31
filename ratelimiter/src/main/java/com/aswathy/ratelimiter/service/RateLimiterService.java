package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final int refillRate;

    public RateLimiterService(
            @Value("${ratelimit.capacity}") int capacity,
            @Value("${ratelimit.refill-rate}") int refillRate
    ) {
        this.capacity = capacity;
        this.refillRate = refillRate;

        // --- DEBUG PRINT ---
        System.out.println("==================================");
        System.out.println("RATE LIMITER CONFIG LOADED:");
        System.out.println("Capacity: " + capacity);
        System.out.println("Refill Rate: " + refillRate);
        System.out.println("==================================");
    }

    public boolean isAllowed(String apiKey) {
        TokenBucket bucket = buckets.computeIfAbsent(apiKey, key -> createNewBucket());
        return bucket.tryConsume();
    }

    private TokenBucket createNewBucket() {
        return new TokenBucket(capacity, refillRate);
    }
}