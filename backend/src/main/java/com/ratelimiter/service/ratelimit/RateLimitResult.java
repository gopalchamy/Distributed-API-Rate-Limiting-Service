package com.ratelimiter.service.ratelimit;

public class RateLimitResult {
    private final boolean allowed;
    private final int limit;
    private final int remaining;
    private final long resetTimeSeconds;
    private final long retryAfterSeconds;
    private final String backendType; // "REDIS" or "IN_MEMORY"

    public RateLimitResult(boolean allowed, int limit, int remaining, long resetTimeSeconds, long retryAfterSeconds, String backendType) {
        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.resetTimeSeconds = resetTimeSeconds;
        this.retryAfterSeconds = retryAfterSeconds;
        this.backendType = backendType;
    }

    public static RateLimitResult allow(int limit, int remaining, long resetTimeSeconds, String backendType) {
        return new RateLimitResult(true, limit, remaining, resetTimeSeconds, 0, backendType);
    }

    public static RateLimitResult reject(int limit, int remaining, long resetTimeSeconds, long retryAfterSeconds, String backendType) {
        return new RateLimitResult(false, limit, remaining, resetTimeSeconds, retryAfterSeconds, backendType);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getResetTimeSeconds() {
        return resetTimeSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public String getBackendType() {
        return backendType;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", limit=" + limit +
                ", remaining=" + remaining +
                ", resetTimeSeconds=" + resetTimeSeconds +
                ", retryAfterSeconds=" + retryAfterSeconds +
                ", backendType='" + backendType + '\'' +
                '}';
    }
}
