package com.ratelimiter.service.ratelimit;

public interface RateLimiterStrategy {
    /**
     * Attempts to acquire tokens for a given rate limit key.
     *
     * @param key the rate limit target key
     * @param capacity maximum burst tokens
     * @param refillTokens tokens replenished per period
     * @param refillPeriodSeconds period in seconds
     * @param requestedTokens tokens required for this request (usually 1)
     * @return RateLimitResult indicating allowed/rejected and token metrics
     */
    RateLimitResult tryAcquire(RateLimitKey key, int capacity, int refillTokens, int refillPeriodSeconds, int requestedTokens);

    /**
     * Checks if this strategy is currently available and healthy (e.g. Redis is up).
     */
    boolean isAvailable();

    /**
     * Strategy identifier (e.g. "REDIS", "IN_MEMORY").
     */
    String getName();
}
