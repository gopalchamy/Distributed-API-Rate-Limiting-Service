package com.ratelimiter;

import com.ratelimiter.service.ratelimit.InMemoryTokenBucketRateLimiter;
import com.ratelimiter.service.ratelimit.RateLimitKey;
import com.ratelimiter.service.ratelimit.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTokenBucketRateLimiterTest {

    private InMemoryTokenBucketRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        rateLimiter = new InMemoryTokenBucketRateLimiter();
    }

    @Test
    public void testTokenAcquisitionWithinCapacity() {
        RateLimitKey key = new RateLimitKey("API_KEY", "test_key_1", "/api/v1/gateway/products");
        int capacity = 5;
        int refillTokens = 5;
        int refillPeriodSeconds = 60;

        // Acquire 5 tokens
        for (int i = 0; i < 5; i++) {
            RateLimitResult result = rateLimiter.tryAcquire(key, capacity, refillTokens, refillPeriodSeconds, 1);
            assertTrue(result.isAllowed(), "Request " + (i + 1) + " should be allowed");
            assertEquals(5 - 1 - i, result.getRemaining());
        }

        // 6th token should be rejected
        RateLimitResult blocked = rateLimiter.tryAcquire(key, capacity, refillTokens, refillPeriodSeconds, 1);
        assertFalse(blocked.isAllowed(), "6th request should be blocked");
        assertEquals(0, blocked.getRemaining());
        assertTrue(blocked.getRetryAfterSeconds() > 0, "Retry-After should be positive");
    }

    @Test
    public void testKeyIsolation() {
        RateLimitKey key1 = new RateLimitKey("API_KEY", "client_A", "/products");
        RateLimitKey key2 = new RateLimitKey("API_KEY", "client_B", "/products");

        // Drain client A
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.tryAcquire(key1, 3, 3, 60, 1).isAllowed());
        }
        assertFalse(rateLimiter.tryAcquire(key1, 3, 3, 60, 1).isAllowed());

        // Client B must still have full tokens
        RateLimitResult clientBResult = rateLimiter.tryAcquire(key2, 3, 3, 60, 1);
        assertTrue(clientBResult.isAllowed());
        assertEquals(2, clientBResult.getRemaining());
    }
}
