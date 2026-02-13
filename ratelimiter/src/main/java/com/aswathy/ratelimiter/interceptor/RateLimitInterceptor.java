package com.aswathy.ratelimiter.interceptor;

import com.aswathy.ratelimiter.model.RateLimitResult;
import com.aswathy.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("Missing X-API-KEY header");
            return false;
        }

        // 1. Get the detailed report card from the Service
        RateLimitResult result = rateLimiterService.isAllowed(apiKey);

        // 2. Add the "Remaining Tokens" header to EVERY response (Allowed or Blocked)
        // This is crucial for "Observability" - the user knows exactly where they stand.
        response.addHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()));

        // 3. Check if they are allowed
        if (!result.isAllowed()) {
            response.setStatus(429); // Too Many Requests

            // Add a "Retry-After" header (Simple suggestion: 1 second)
            response.addHeader("X-RateLimit-Retry-After-Seconds", "1");

            response.getWriter().write("Too many requests. Please wait.");
            return false;
        }

        return true;
    }
}