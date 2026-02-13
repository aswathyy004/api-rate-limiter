package com.aswathy.ratelimiter.model;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

public class RedisFixedWindowRateLimiter implements RateLimiter {

    private final String apiKey;
    private final int limit;
    private final StringRedisTemplate redisTemplate;

    public RedisFixedWindowRateLimiter(String apiKey, int limit, StringRedisTemplate redisTemplate) {
        this.apiKey = apiKey;
        this.limit = limit;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult tryConsume() {
        // 1. Create a unique key for this user in Redis (e.g., "rate_limit:dist-user")
        // The prefix "rate_limit:" helps group these keys together in the database.
        String key = "rate_limit:" + apiKey;

        try {
            // 2. Ask Redis to increment the counter.
            // This is Atomic! Even if 10 servers run this line at the exact same millisecond,
            // Redis will queue them and count 1, 2, 3... perfectly.
            Long currentCount = redisTemplate.opsForValue().increment(key);

            // 3. If this is the FIRST request (count == 1), set the expiration (TTL)
            // We set it to expire in 1 minute (Fixed Window).
            if (currentCount != null && currentCount == 1) {
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            // 4. Calculate remaining tokens
            // Handle null safely (though increment usually returns a number)
            long count = (currentCount != null) ? currentCount : limit + 1;
            long remaining = Math.max(0, limit - count);

            if (count <= limit) {
                return new RateLimitResult(true, remaining);
            } else {
                return new RateLimitResult(false, 0);
            }
        } catch (Exception e) {
            // Fail Open: If Redis is down (e.g., Docker crashes), we allow the request.
            // This prevents your entire API from breaking just because the rate limiter failed.
            System.err.println("Redis is unavailable: " + e.getMessage());
            return new RateLimitResult(true, limit);
        }
    }
}
