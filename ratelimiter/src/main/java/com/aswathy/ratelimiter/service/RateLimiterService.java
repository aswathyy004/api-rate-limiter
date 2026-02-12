package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.FixedWindowCounter;
import com.aswathy.ratelimiter.model.RateLimiter;
import com.aswathy.ratelimiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Key Change 1: The Map now holds the generic 'RateLimiter' interface
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

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
        // Key Change 2: We use the generic interface methods
        RateLimiter limiter = limiters.computeIfAbsent(apiKey, this::createLimiter);
        return limiter.tryConsume();
    }

    private RateLimiter createLimiter(String apiKey) {
        // --- ALGORITHM STRATEGY ---

        // Tier 1: Premium Users -> Token Bucket (Smooth, allows bursts)
        if (apiKey.startsWith("prem-")) {
            System.out.println("Creating Token Bucket for VIP: " + apiKey);
            return new TokenBucket(20, 2);
        }

        // Tier 2: Free Users -> Fixed Window (Strict, simple limits)
        // Let's say: 2 requests per 10 seconds (Strict!)
        if (apiKey.startsWith("free-")) {
            System.out.println("Creating Fixed Window for Free user: " + apiKey);
            return new FixedWindowCounter(2, 10000); // 2 reqs / 10 sec window
        }

        // Tier 3: Default -> Token Bucket
        return new TokenBucket(defaultCapacity, defaultRefillRate);
    }
}