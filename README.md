# API Rate Limiter & Access Control System

A high-performance, thread-safe rate limiting middleware developed with Java and Spring Boot. This project applies the Token Bucket Algorithm to manage traffic flow and mitigate API abuse, ensuring system stability even under heavy load.

# Main Features :

Token Bucket Algorithm: Customized implementation with AtomicLong for thread-safe, lock-free concurrency.

Scalable Architecture: Utilizes a Service layer (ConcurrentHashMap) to handle rate limits for thousands of unique users.

Interceptor-Based Security: Segregates security logic from business logic with Spring HandlerInterceptor.

Configurable Limits: Rate limits (capacity/refill rate) are set in application.properties without modifying code.

Automated Testing: Tested with JUnit 5.

# Tech Stack :

Java 17

Spring Boot 3 (Web, Validation)

Maven

JUnit 5

# How It Works

Request: Client makes a request with an X-API-KEY header.

Interceptor: The application intercepts the request before it reaches the Controller.

Bucket Check: The RateLimiterService looks up the user's own Token Bucket.

Decision:

If tokens are available -> Request passes (HTTP 200).

If bucket is empty -> Request blocked (HTTP 429 Too Many Requests).

# How to Run

Clone the repository:

git clone [https://github.com/aswathyy004/api-rate-limiter.git](https://github.com/aswathyy004/api-rate-limiter.git)

Run the application:
Open the project in IntelliJ IDEA and run RatelimiterApplication.

Test with curl:

🔺 Success

curl.exe -H "X-API-KEY: test-user" http://localhost:8080/api/greet

🔺 To trigger rate limit (Windows PowerShell chain command)

curl.exe -H "X-API-KEY: test" http://localhost:8080/api/greet; curl.exe -H "X-API-KEY: test" http://localhost:8080/api/greet


# Testing

Run the JUnit tests to verify the blocking logic:
src/test/java/com/aswathy/ratelimiter/service/RateLimiterServiceTest.java

Built by Aswathy as a Backend Engineering Portfolio Project.