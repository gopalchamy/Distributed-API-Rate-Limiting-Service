package com.ratelimiter.dto;

import com.ratelimiter.model.enums.PlanTier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RuleRequests {

    public static class CreateRuleRequest {
        private Long applicationId;
        private PlanTier planTier;

        @NotBlank(message = "Endpoint pattern is required")
        private String endpointPattern = "/**";

        private String httpMethod = "ALL";

        @Min(value = 1, message = "Capacity must be at least 1")
        private int capacity = 60;

        @Min(value = 1, message = "Refill tokens must be at least 1")
        private int refillTokens = 60;

        @Min(value = 1, message = "Refill period seconds must be at least 1")
        private int refillPeriodSeconds = 60;

        private String description;

        public CreateRuleRequest() {}

        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public PlanTier getPlanTier() { return planTier; }
        public void setPlanTier(PlanTier planTier) { this.planTier = planTier; }
        public String getEndpointPattern() { return endpointPattern; }
        public void setEndpointPattern(String endpointPattern) { this.endpointPattern = endpointPattern; }
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public int getRefillTokens() { return refillTokens; }
        public void setRefillTokens(int refillTokens) { this.refillTokens = refillTokens; }
        public int getRefillPeriodSeconds() { return refillPeriodSeconds; }
        public void setRefillPeriodSeconds(int refillPeriodSeconds) { this.refillPeriodSeconds = refillPeriodSeconds; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RateLimitRuleDto {
        private Long id;
        private Long applicationId;
        private PlanTier planTier;
        private String endpointPattern;
        private String httpMethod;
        private int capacity;
        private int refillTokens;
        private int refillPeriodSeconds;
        private boolean active;
        private String description;
        private String createdAt;

        public RateLimitRuleDto(Long id, Long applicationId, PlanTier planTier, String endpointPattern,
                                String httpMethod, int capacity, int refillTokens, int refillPeriodSeconds,
                                boolean active, String description, String createdAt) {
            this.id = id;
            this.applicationId = applicationId;
            this.planTier = planTier;
            this.endpointPattern = endpointPattern;
            this.httpMethod = httpMethod;
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriodSeconds = refillPeriodSeconds;
            this.active = active;
            this.description = description;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getApplicationId() { return applicationId; }
        public PlanTier getPlanTier() { return planTier; }
        public String getEndpointPattern() { return endpointPattern; }
        public String getHttpMethod() { return httpMethod; }
        public int getCapacity() { return capacity; }
        public int getRefillTokens() { return refillTokens; }
        public int getRefillPeriodSeconds() { return refillPeriodSeconds; }
        public boolean isActive() { return active; }
        public String getDescription() { return description; }
        public String getCreatedAt() { return createdAt; }
    }
}
