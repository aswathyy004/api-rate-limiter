package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // The "Coat Check Room": Stores a bucket for every API Key / User ID
    // Key = API Key, Value = TokenBucket
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String apiKey) {
        // 1. Get the bucket for this specific API key
        // computeIfAbsent means: "If this user doesn't have a bucket yet, make one now."
        TokenBucket bucket = buckets.computeIfAbsent(apiKey, key -> createNewBucket());

        // 2. Try to take a token from *their* bucket
        return bucket.tryConsume();
    }

    private TokenBucket createNewBucket() {
        // Standard limit: 10 requests burst, refilling at 1 token/sec
        return new TokenBucket(10, 1);
    }
}