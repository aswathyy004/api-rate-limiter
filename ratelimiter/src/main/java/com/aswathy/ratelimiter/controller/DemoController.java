package com.aswathy.ratelimiter.controller;

import com.aswathy.ratelimiter.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    private final RateLimiterService rateLimiterService;

    // Spring Boot automatically injects the Service here
    public DemoController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/greet")
    public ResponseEntity<Map<String, String>> greet() {

        // For now, we pretend every request comes from the same "demo-api-key".
        // In the next step, we will extract this from the request header!
        String simulatedApiKey = "demo-api-key";

        boolean allowed = rateLimiterService.isAllowed(simulatedApiKey);

        if (allowed) {
            return ResponseEntity.ok(Map.of("message", "Request Successful!", "status", "Allowed"));
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many requests! Wait a moment.", "status", "Blocked"));
        }
    }
}