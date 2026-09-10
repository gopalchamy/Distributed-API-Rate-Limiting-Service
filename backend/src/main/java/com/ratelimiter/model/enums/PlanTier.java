package com.ratelimiter.model.enums;

public enum PlanTier {
    FREE(20, 20, 60),          // 20 requests per 60 seconds
    PRO(120, 120, 60),         // 120 requests per 60 seconds
    ENTERPRISE(600, 600, 60);  // 600 requests per 60 seconds

    private final int defaultCapacity;
    private final int defaultRefillTokens;
    private final int defaultRefillPeriodSeconds;

    PlanTier(int defaultCapacity, int defaultRefillTokens, int defaultRefillPeriodSeconds) {
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillTokens = defaultRefillTokens;
        this.defaultRefillPeriodSeconds = defaultRefillPeriodSeconds;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public int getDefaultRefillTokens() {
        return defaultRefillTokens;
    }

    public int getDefaultRefillPeriodSeconds() {
        return defaultRefillPeriodSeconds;
    }
}
