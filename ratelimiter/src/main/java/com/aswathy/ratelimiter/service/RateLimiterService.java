package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // These defaults come from application.properties (Capacity=1)
    private final int defaultCapacity;
    private final int defaultRefillRate;

    public RateLimiterService(
            @Value("${ratelimit.capacity}") int defaultCapacity,
            @Value("${ratelimit.refill-rate}") int defaultRefillRate
    ) {
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillRate = defaultRefillRate;
    }

    public boolean isAllowed(String apiKey) {
        // We pass the 'apiKey' into the createNewBucket method now!
        TokenBucket bucket = buckets.computeIfAbsent(apiKey, this::createNewBucket);
        return bucket.tryConsume();
    }

    private TokenBucket createNewBucket(String apiKey) {
        // --- BUSINESS LOGIC HERE ---

        // Tier 1: Premium Users (Start with "prem-")
        if (apiKey.startsWith("prem-")) {
            System.out.println("Creating Premium Bucket for: " + apiKey);
            return new TokenBucket(20, 2); // 20 tokens, 2 per second
        }

        // Tier 2: Free Users (Start with "free-")
        if (apiKey.startsWith("free-")) {
            System.out.println("Creating Free Bucket for: " + apiKey);
            return new TokenBucket(5, 1);  // 5 tokens, 1 per second
        }

        // Tier 3: Everyone else (Uses the strict settings from properties file)
        System.out.println("Creating Default Bucket for: " + apiKey);
        return new TokenBucket(defaultCapacity, defaultRefillRate);
    }
}