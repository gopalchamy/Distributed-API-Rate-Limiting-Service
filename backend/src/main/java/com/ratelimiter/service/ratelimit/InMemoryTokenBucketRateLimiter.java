package com.ratelimiter.service.ratelimit;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class InMemoryTokenBucketRateLimiter implements RateLimiterStrategy {

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    private static class BucketState {
        double tokens;
        long lastUpdatedMillis;

        BucketState(double tokens, long lastUpdatedMillis) {
            this.tokens = tokens;
            this.lastUpdatedMillis = lastUpdatedMillis;
        }
    }

    @Override
    public synchronized RateLimitResult tryAcquire(RateLimitKey key, int capacity, int refillTokens, int refillPeriodSeconds, int requestedTokens) {
        String cacheKey = key.toRedisKey();
        long now = System.currentTimeMillis();
        double refillRate = (double) refillTokens / refillPeriodSeconds;

        BucketState state = buckets.compute(cacheKey, (k, existing) -> {
            if (existing == null) {
                return new BucketState(capacity, now);
            }
            double elapsedSeconds = Math.max(0.0, (now - existing.lastUpdatedMillis) / 1000.0);
            double refilled = existing.tokens + (elapsedSeconds * refillRate);
            double currentTokens = Math.min((double) capacity, refilled);
            return new BucketState(currentTokens, now);
        });

        long resetSeconds = Math.max(1, (long) Math.ceil((capacity - state.tokens) / refillRate));

        if (state.tokens >= requestedTokens) {
            state.tokens -= requestedTokens;
            int remaining = (int) Math.floor(state.tokens);
            return RateLimitResult.allow(capacity, remaining, resetSeconds, getName());
        } else {
            int remaining = (int) Math.floor(state.tokens);
            double needed = requestedTokens - state.tokens;
            long retryAfter = Math.max(1, (long) Math.ceil(needed / refillRate));
            return RateLimitResult.reject(capacity, remaining, resetSeconds, retryAfter, getName());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "IN_MEMORY";
    }

    /**
     * Clear all cached buckets (useful for test resets).
     */
    public void reset() {
        buckets.clear();
    }
}
