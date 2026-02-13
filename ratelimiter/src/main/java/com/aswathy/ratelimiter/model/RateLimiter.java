package com.aswathy.ratelimiter.model;

public interface RateLimiter {
    // OLD: boolean tryConsume();
    // NEW: We return the full report card!
    RateLimitResult tryConsume();
}