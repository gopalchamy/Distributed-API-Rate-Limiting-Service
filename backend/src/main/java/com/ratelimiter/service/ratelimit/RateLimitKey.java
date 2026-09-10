package com.ratelimiter.service.ratelimit;

import java.util.Objects;

public class RateLimitKey {
    private final String identifierType; // "API_KEY", "CLIENT_IP", "USER_ID"
    private final String identifier;     // e.g. "rl_live_abc123" or "192.168.1.1"
    private final String endpoint;       // e.g. "/api/v1/gateway/products" or "GLOBAL"

    public RateLimitKey(String identifierType, String identifier, String endpoint) {
        this.identifierType = identifierType;
        this.identifier = identifier;
        this.endpoint = endpoint != null ? endpoint : "GLOBAL";
    }

    public String toRedisKey() {
        return String.format("rl:%s:%s:%s", identifierType.toLowerCase(), identifier, endpoint);
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimitKey that = (RateLimitKey) o;
        return Objects.equals(identifierType, that.identifierType) &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierType, identifier, endpoint);
    }

    @Override
    public String toString() {
        return toRedisKey();
    }
}
