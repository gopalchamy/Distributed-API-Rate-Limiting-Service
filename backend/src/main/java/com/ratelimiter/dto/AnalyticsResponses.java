package com.ratelimiter.dto;

import java.util.List;

public class AnalyticsResponses {

    public static class DashboardSummaryResponse {
        private long totalRequests;
        private long allowedRequests;
        private long blockedRequests;
        private double blockPercentage;
        private double averageLatencyMs;
        private int totalApps;
        private int activeKeys;
        private String rateLimiterEngine; // "REDIS" or "IN_MEMORY"
        private List<TrafficTimelinePoint> timeline;
        private List<EndpointBreakdown> topEndpoints;

        public DashboardSummaryResponse(long totalRequests, long allowedRequests, long blockedRequests,
                                        double blockPercentage, double averageLatencyMs, int totalApps,
                                        int activeKeys, String rateLimiterEngine,
                                        List<TrafficTimelinePoint> timeline,
                                        List<EndpointBreakdown> topEndpoints) {
            this.totalRequests = totalRequests;
            this.allowedRequests = allowedRequests;
            this.blockedRequests = blockedRequests;
            this.blockPercentage = blockPercentage;
            this.averageLatencyMs = averageLatencyMs;
            this.totalApps = totalApps;
            this.activeKeys = activeKeys;
            this.rateLimiterEngine = rateLimiterEngine;
            this.timeline = timeline;
            this.topEndpoints = topEndpoints;
        }

        public long getTotalRequests() { return totalRequests; }
        public long getAllowedRequests() { return allowedRequests; }
        public long getBlockedRequests() { return blockedRequests; }
        public double getBlockPercentage() { return blockPercentage; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public int getTotalApps() { return totalApps; }
        public int getActiveKeys() { return activeKeys; }
        public String getRateLimiterEngine() { return rateLimiterEngine; }
        public List<TrafficTimelinePoint> getTimeline() { return timeline; }
        public List<EndpointBreakdown> getTopEndpoints() { return topEndpoints; }
    }

    public static class TrafficTimelinePoint {
        private String timestamp;
        private long allowed;
        private long blocked;

        public TrafficTimelinePoint(String timestamp, long allowed, long blocked) {
            this.timestamp = timestamp;
            this.allowed = allowed;
            this.blocked = blocked;
        }

        public String getTimestamp() { return timestamp; }
        public long getAllowed() { return allowed; }
        public long getBlocked() { return blocked; }
    }

    public static class EndpointBreakdown {
        private String endpoint;
        private String method;
        private long count;
        private long blocked;

        public EndpointBreakdown(String endpoint, String method, long count, long blocked) {
            this.endpoint = endpoint;
            this.method = method;
            this.count = count;
            this.blocked = blocked;
        }

        public String getEndpoint() { return endpoint; }
        public String getMethod() { return method; }
        public long getCount() { return count; }
        public long getBlocked() { return blocked; }
    }

    public static class LogEntryDto {
        private Long id;
        private Long applicationId;
        private String apiKeyPrefix;
        private String endpoint;
        private String httpMethod;
        private int httpStatus;
        private boolean allowed;
        private String clientIp;
        private long latencyMs;
        private int remainingTokens;
        private String timestamp;

        public LogEntryDto(Long id, Long applicationId, String apiKeyPrefix, String endpoint, String httpMethod,
                           int httpStatus, boolean allowed, String clientIp, long latencyMs,
                           int remainingTokens, String timestamp) {
            this.id = id;
            this.applicationId = applicationId;
            this.apiKeyPrefix = apiKeyPrefix;
            this.endpoint = endpoint;
            this.httpMethod = httpMethod;
            this.httpStatus = httpStatus;
            this.allowed = allowed;
            this.clientIp = clientIp;
            this.latencyMs = latencyMs;
            this.remainingTokens = remainingTokens;
            this.timestamp = timestamp;
        }

        public Long getId() { return id; }
        public Long getApplicationId() { return applicationId; }
        public String getApiKeyPrefix() { return apiKeyPrefix; }
        public String getEndpoint() { return endpoint; }
        public String getHttpMethod() { return httpMethod; }
        public int getHttpStatus() { return httpStatus; }
        public boolean isAllowed() { return allowed; }
        public String getClientIp() { return clientIp; }
        public long getLatencyMs() { return latencyMs; }
        public int getRemainingTokens() { return remainingTokens; }
        public String getTimestamp() { return timestamp; }
    }
}
