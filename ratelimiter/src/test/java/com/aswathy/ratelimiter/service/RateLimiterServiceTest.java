package com.aswathy.ratelimiter.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    @Test
    void shouldBlockRequestWhenBucketIsEmpty() {
        // Setup: We create a service with Capacity = 1, Refill = 1
        // This simulates the "strict" mode manually without needing application.properties
        RateLimiterService service = new RateLimiterService(1, 1);
        String apiKey = "test-user";

        // Action 1: First request (Should be Allowed)
        assertTrue(service.isAllowed(apiKey), "First request should be allowed");

        // Action 2: Second request IMMEDIATELY (Should be Blocked)
        // Since this runs in milliseconds, the bucket won't have time to refill!
        assertFalse(service.isAllowed(apiKey), "Second request should be blocked immediately");
    }

    @Test
    void distinctUsersShouldHaveSeparateBuckets() {
        RateLimiterService service = new RateLimiterService(1, 1);

        // User A uses their only token
        assertTrue(service.isAllowed("User-A"));
        assertFalse(service.isAllowed("User-A")); // Blocked

        // User B should still be allowed (they have their own bucket)
        assertTrue(service.isAllowed("User-B"));
    }
}