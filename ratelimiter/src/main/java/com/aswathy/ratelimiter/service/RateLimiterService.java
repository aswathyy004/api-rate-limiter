package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.FixedWindowCounter;
import com.aswathy.ratelimiter.model.RateLimitResult;
import com.aswathy.ratelimiter.model.RateLimiter;
import com.aswathy.ratelimiter.model.TokenBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

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

    // UPDATE: We now return the full report (RateLimitResult) instead of just boolean
    public RateLimitResult isAllowed(String apiKey) {
        RateLimiter limiter = limiters.computeIfAbsent(apiKey, this::createLimiter);
        return limiter.tryConsume();
    }

    private RateLimiter createLimiter(String apiKey) {
        // Tier 1: Premium Users -> Token Bucket
        if (apiKey.startsWith("prem-")) {
            logger.info("Creating Token Bucket for VIP user: {}", apiKey);
            return new TokenBucket(20, 2);
        }

        // Tier 2: Free Users -> Fixed Window
        if (apiKey.startsWith("free-")) {
            logger.info("Creating Fixed Window for Free user: {}", apiKey);
            return new FixedWindowCounter(2, 10000); // 2 reqs / 10 sec window
        }

        // Tier 3: Default -> Token Bucket
        logger.info("Creating Default Bucket for user: {}", apiKey);
        return new TokenBucket(defaultCapacity, defaultRefillRate);
    }
}