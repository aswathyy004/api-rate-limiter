package com.aswathy.ratelimiter.model;

/**
 * A generic interface for all rate limiting algorithms.
 * Whether it's Token Bucket, Fixed Window, or Sliding Window,
 * they all must answer one simple question: "Can I consume a request?"
 */
public interface RateLimiter {
    boolean tryConsume();
}