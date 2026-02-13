package com.aswathy.ratelimiter.service;

import com.aswathy.ratelimiter.model.RateLimitResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    @Test
    void shouldBlockRequestWhenBucketIsEmpty() {
        // Setup: Capacity = 1, Refill = 1
        RateLimiterService service = new RateLimiterService(1, 1);
        String apiKey = "test-user";

        // Action 1: First request
        RateLimitResult result1 = service.isAllowed(apiKey);

        // Assert: Should be allowed, and 0 tokens left (since we used the only one)
        assertTrue(result1.isAllowed(), "First request should be allowed");
        assertEquals(0, result1.getRemainingTokens(), "Should have 0 tokens remaining");

        // Action 2: Second request IMMEDIATELY
        RateLimitResult result2 = service.isAllowed(apiKey);

        // Assert: Should be blocked
        assertFalse(result2.isAllowed(), "Second request should be blocked immediately");
        assertEquals(0, result2.getRemainingTokens(), "Should still have 0 tokens");
    }

    @Test
    void distinctUsersShouldHaveSeparateBuckets() {
        RateLimiterService service = new RateLimiterService(1, 1);

        // User A uses their only token
        assertTrue(service.isAllowed("User-A").isAllowed());
        assertFalse(service.isAllowed("User-A").isAllowed()); // Blocked

        // User B should still be allowed (they have their own bucket)
        assertTrue(service.isAllowed("User-B").isAllowed());
    }
}