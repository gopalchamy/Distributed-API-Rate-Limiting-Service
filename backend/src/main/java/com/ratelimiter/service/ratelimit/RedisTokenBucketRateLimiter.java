package com.ratelimiter.service.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiterStrategy {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBucketRateLimiter.class);

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> tokenBucketScript;
    private volatile boolean redisAvailable = true;

    public RedisTokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        this.tokenBucketScript.setResultType(List.class);
    }

    @Override
    public RateLimitResult tryAcquire(RateLimitKey key, int capacity, int refillTokens, int refillPeriodSeconds, int requestedTokens) {
        try {
            long now = System.currentTimeMillis();
            String redisKey = key.toRedisKey();

            List<?> result = redisTemplate.execute(
                    tokenBucketScript,
                    Collections.singletonList(redisKey),
                    String.valueOf(capacity),
                    String.valueOf(refillTokens),
                    String.valueOf(refillPeriodSeconds),
                    String.valueOf(requestedTokens),
                    String.valueOf(now)
            );

            if (result != null && result.size() >= 4) {
                long allowedVal = ((Number) result.get(0)).longValue();
                int remaining = ((Number) result.get(1)).intValue();
                long resetSeconds = ((Number) result.get(2)).longValue();
                long retryAfter = ((Number) result.get(3)).longValue();

                redisAvailable = true;
                if (allowedVal == 1) {
                    return RateLimitResult.allow(capacity, remaining, resetSeconds, getName());
                } else {
                    return RateLimitResult.reject(capacity, remaining, resetSeconds, retryAfter, getName());
                }
            }

            // If unexpected null result, mark unavailable
            return RateLimitResult.allow(capacity, capacity - 1, refillPeriodSeconds, getName());
        } catch (Exception ex) {
            if (redisAvailable) {
                log.warn("Redis is unreachable ({}: {}). Failing over to in-memory rate limiter.", ex.getClass().getSimpleName(), ex.getMessage());
                redisAvailable = false;
            }
            throw new RedisUnavailableException("Redis unavailable: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isAvailable() {
        if (!redisAvailable) {
            // Attempt a quick ping test periodically
            try {
                if (redisTemplate.getConnectionFactory() != null) {
                    String ping = redisTemplate.getConnectionFactory().getConnection().ping();
                    redisAvailable = "PONG".equalsIgnoreCase(ping);
                }
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        return redisAvailable;
    }

    @Override
    public String getName() {
        return "REDIS";
    }

    public static class RedisUnavailableException extends RuntimeException {
        public RedisUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
