package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.FixedWindowCounter;
import com.aswathy.ratelimiter.model.RateLimitResult;
import com.aswathy.ratelimiter.model.RateLimiter;
import com.aswathy.ratelimiter.model.RedisFixedWindowRateLimiter;
import com.aswathy.ratelimiter.model.TokenBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    // We still keep the local map to cache the objects,
    // but for Redis limiters, the object just holds the connection logic.
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    // The connector to our Docker Redis
    private final StringRedisTemplate redisTemplate;

    private final int defaultCapacity;
    private final int defaultRefillRate;

    // Inject StringRedisTemplate here
    public RateLimiterService(
            StringRedisTemplate redisTemplate,
            @Value("${ratelimit.capacity}") int defaultCapacity,
            @Value("${ratelimit.refill-rate}") int defaultRefillRate
    ) {
        this.redisTemplate = redisTemplate;
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillRate = defaultRefillRate;
    }

    public RateLimitResult isAllowed(String apiKey) {
        RateLimiter limiter = limiters.computeIfAbsent(apiKey, this::createLimiter);
        return limiter.tryConsume();
    }

    private RateLimiter createLimiter(String apiKey) {
        // --- TIER 1: Distributed Users (Redis) ---
        // Any key starting with "dist-" will use the external database
        if (apiKey.startsWith("dist-")) {
            logger.info("Creating Redis Rate Limiter for: {}", apiKey);
            // Limit: 10 requests per minute
            return new RedisFixedWindowRateLimiter(apiKey, 10, redisTemplate);
        }

        // --- TIER 2: Premium Users (Local Memory) ---
        if (apiKey.startsWith("prem-")) {
            logger.info("Creating Token Bucket for VIP user: {}", apiKey);
            return new TokenBucket(20, 2);
        }

        // --- TIER 3: Free Users (Local Memory) ---
        if (apiKey.startsWith("free-")) {
            logger.info("Creating Fixed Window for Free user: {}", apiKey);
            return new FixedWindowCounter(2, 10000);
        }

        // --- TIER 4: Default (Local Memory) ---
        logger.info("Creating Default Bucket for user: {}", apiKey);
        return new TokenBucket(defaultCapacity, defaultRefillRate);
    }
}