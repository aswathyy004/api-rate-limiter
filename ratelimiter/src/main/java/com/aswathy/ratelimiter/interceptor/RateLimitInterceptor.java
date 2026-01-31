package com.aswathy.ratelimiter.interceptor;

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
        // 1. Extract the API Key from the header
        String apiKey = request.getHeader("X-API-KEY");

        // 2. Validate: Is the key missing or empty?
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(400); // Bad Request
            response.getWriter().write("Missing X-API-KEY header");
            return false; // Block the request
        }

        // 3. Ask the RateLimiterService: Is this key allowed?
        boolean allowed = rateLimiterService.isAllowed(apiKey);

        if (!allowed) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Too many requests");
            return false; // Block the request
        }

        // 4. If we got here, everything is good! Let the request pass to the Controller.
        return true;
    }
}