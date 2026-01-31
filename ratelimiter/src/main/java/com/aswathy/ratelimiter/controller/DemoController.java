package com.aswathy.ratelimiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    // No Service needed here!
    // The RateLimitInterceptor (the "Guard") checks the ID at the door.
    // If the code reaches here, the user is already approved.

    @GetMapping("/api/greet")
    public ResponseEntity<Map<String, String>> greet() {
        return ResponseEntity.ok(Map.of(
                "status", "Allowed",
                "message", "Hello! You made it past the security guard."
        ));
    }
}