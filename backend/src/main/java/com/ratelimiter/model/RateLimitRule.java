package com.ratelimiter.model;

import com.ratelimiter.model.enums.PlanTier;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rate_limit_rules")
public class RateLimitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // If applicationId is null, rule is a global / plan default rule
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    private PlanTier planTier; // Optional tier override

    @Column(nullable = false)
    private String endpointPattern = "/**"; // e.g. "/api/v1/gateway/products" or "/**"

    @Column(nullable = false)
    private String httpMethod = "ALL"; // GET, POST, PUT, DELETE, or ALL

    @Column(nullable = false)
    private int capacity = 60; // Bucket maximum tokens (burst limit)

    @Column(nullable = false)
    private int refillTokens = 60; // Tokens added per refill period

    @Column(nullable = false)
    private int refillPeriodSeconds = 60; // Duration of refill period

    @Column(nullable = false)
    private boolean active = true;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RateLimitRule() {}

    public RateLimitRule(Long applicationId, PlanTier planTier, String endpointPattern, String httpMethod,
                         int capacity, int refillTokens, int refillPeriodSeconds, String description) {
        this.applicationId = applicationId;
        this.planTier = planTier;
        this.endpointPattern = endpointPattern != null ? endpointPattern : "/**";
        this.httpMethod = httpMethod != null ? httpMethod : "ALL";
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriodSeconds = refillPeriodSeconds;
        this.description = description;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public PlanTier getPlanTier() {
        return planTier;
    }

    public void setPlanTier(PlanTier planTier) {
        this.planTier = planTier;
    }

    public String getEndpointPattern() {
        return endpointPattern;
    }

    public void setEndpointPattern(String endpointPattern) {
        this.endpointPattern = endpointPattern;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(int refillTokens) {
        this.refillTokens = refillTokens;
    }

    public int getRefillPeriodSeconds() {
        return refillPeriodSeconds;
    }

    public void setRefillPeriodSeconds(int refillPeriodSeconds) {
        this.refillPeriodSeconds = refillPeriodSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
