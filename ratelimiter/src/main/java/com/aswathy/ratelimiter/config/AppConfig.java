package com.aswathy.ratelimiter.config;

import com.aswathy.ratelimiter.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public AppConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply the rate limiter to all API endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}