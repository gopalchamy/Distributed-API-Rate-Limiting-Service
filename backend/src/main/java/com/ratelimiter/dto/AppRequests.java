package com.ratelimiter.dto;

import com.ratelimiter.model.enums.PlanTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AppRequests {

    public static class CreateAppRequest {
        @NotBlank(message = "Application name is required")
        private String name;

        private String description;

        @NotNull(message = "Plan tier is required")
        private PlanTier planTier = PlanTier.FREE;

        public CreateAppRequest() {}

        public CreateAppRequest(String name, String description, PlanTier planTier) {
            this.name = name;
            this.description = description;
            this.planTier = planTier;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public PlanTier getPlanTier() { return planTier; }
        public void setPlanTier(PlanTier planTier) { this.planTier = planTier; }
    }

    public static class GenerateKeyRequest {
        private String keyName;

        public GenerateKeyRequest() {}
        public GenerateKeyRequest(String keyName) { this.keyName = keyName; }

        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }
    }

    public static class ApiKeyDto {
        private Long id;
        private String keyValue; // only present upon creation or for owner
        private String keyPrefix;
        private String name;
        private boolean active;
        private String createdAt;

        public ApiKeyDto(Long id, String keyValue, String keyPrefix, String name, boolean active, String createdAt) {
            this.id = id;
            this.keyValue = keyValue;
            this.keyPrefix = keyPrefix;
            this.name = name;
            this.active = active;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getKeyValue() { return keyValue; }
        public String getKeyPrefix() { return keyPrefix; }
        public String getName() { return name; }
        public boolean isActive() { return active; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class ApplicationDto {
        private Long id;
        private String name;
        private String description;
        private PlanTier planTier;
        private boolean active;
        private String createdAt;
        private List<ApiKeyDto> apiKeys;
        private int rateLimitCapacity;
        private int refillTokens;
        private int refillPeriodSeconds;

        public ApplicationDto(Long id, String name, String description, PlanTier planTier, boolean active,
                              String createdAt, List<ApiKeyDto> apiKeys, int rateLimitCapacity, int refillTokens, int refillPeriodSeconds) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.planTier = planTier;
            this.active = active;
            this.createdAt = createdAt;
            this.apiKeys = apiKeys;
            this.rateLimitCapacity = rateLimitCapacity;
            this.refillTokens = refillTokens;
            this.refillPeriodSeconds = refillPeriodSeconds;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public PlanTier getPlanTier() { return planTier; }
        public boolean isActive() { return active; }
        public String getCreatedAt() { return createdAt; }
        public List<ApiKeyDto> getApiKeys() { return apiKeys; }
        public int getRateLimitCapacity() { return rateLimitCapacity; }
        public int getRefillTokens() { return refillTokens; }
        public int getRefillPeriodSeconds() { return refillPeriodSeconds; }
    }
}
