package com.ratelimiter.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request_audit_logs", indexes = {
    @Index(name = "idx_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_log_app_id", columnList = "applicationId"),
    @Index(name = "idx_log_allowed", columnList = "allowed")
})
public class RequestAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;

    private String apiKeyPrefix;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private int httpStatus;

    @Column(nullable = false)
    private boolean allowed;

    private String clientIp;

    private long latencyMs;

    private int remainingTokens;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public RequestAuditLog() {}

    public RequestAuditLog(Long applicationId, String apiKeyPrefix, String endpoint, String httpMethod,
                           int httpStatus, boolean allowed, String clientIp, long latencyMs, int remainingTokens) {
        this.applicationId = applicationId;
        this.apiKeyPrefix = apiKeyPrefix;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.httpStatus = httpStatus;
        this.allowed = allowed;
        this.clientIp = clientIp;
        this.latencyMs = latencyMs;
        this.remainingTokens = remainingTokens;
        this.timestamp = Instant.now();
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

    public String getApiKeyPrefix() {
        return apiKeyPrefix;
    }

    public void setApiKeyPrefix(String apiKeyPrefix) {
        this.apiKeyPrefix = apiKeyPrefix;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
