package com.ratelimiter.service.analytics;

import com.ratelimiter.dto.AnalyticsResponses;
import com.ratelimiter.model.RequestAuditLog;
import com.ratelimiter.repository.ApiKeyRepository;
import com.ratelimiter.repository.ApplicationRepository;
import com.ratelimiter.repository.RequestAuditLogRepository;
import com.ratelimiter.service.ratelimit.RedisTokenBucketRateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final RequestAuditLogRepository auditLogRepository;
    private final ApplicationRepository applicationRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final RedisTokenBucketRateLimiter redisRateLimiter;

    public AnalyticsService(RequestAuditLogRepository auditLogRepository,
                            ApplicationRepository applicationRepository,
                            ApiKeyRepository apiKeyRepository,
                            RedisTokenBucketRateLimiter redisRateLimiter) {
        this.auditLogRepository = auditLogRepository;
        this.applicationRepository = applicationRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.redisRateLimiter = redisRateLimiter;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponses.DashboardSummaryResponse getDashboardSummary() {
        Instant since24h = Instant.now().minus(24, ChronoUnit.HOURS);

        long total = auditLogRepository.countTotalRequestsSince(since24h);
        long allowed = auditLogRepository.countAllowedRequestsSince(since24h);
        long blocked = auditLogRepository.countBlockedRequestsSince(since24h);
        Double avgLatency = auditLogRepository.getAverageLatencySince(since24h);
        int totalApps = (int) applicationRepository.count();
        int activeKeys = (int) apiKeyRepository.count();

        double blockPercentage = total > 0 ? ((double) blocked / total) * 100.0 : 0.0;
        String engine = redisRateLimiter.isAvailable() ? "REDIS (Distributed)" : "IN_MEMORY (Standalone)";

        // Generate 12 timeline points for the last 2 hours
        List<AnalyticsResponses.TrafficTimelinePoint> timeline = buildTimeline();

        // Top endpoints
        List<AnalyticsResponses.EndpointBreakdown> topEndpoints = buildEndpointBreakdowns();

        return new AnalyticsResponses.DashboardSummaryResponse(
                total,
                allowed,
                blocked,
                Math.round(blockPercentage * 10.0) / 10.0,
                avgLatency != null ? Math.round(avgLatency * 10.0) / 10.0 : 0.0,
                totalApps,
                activeKeys,
                engine,
                timeline,
                topEndpoints
        );
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsResponses.LogEntryDto> getAuditLogs(Long appId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<RequestAuditLog> logs;

        if (appId != null) {
            logs = auditLogRepository.findByApplicationIdOrderByTimestampDesc(appId, pageRequest);
        } else {
            logs = auditLogRepository.findAllByOrderByTimestampDesc(pageRequest);
        }

        return logs.map(this::toLogDto);
    }

    private List<AnalyticsResponses.TrafficTimelinePoint> buildTimeline() {
        List<RequestAuditLog> recentLogs = auditLogRepository.findTop50ByOrderByTimestampDesc();
        // Bucket logs by 5-minute intervals or create representative timeline
        Map<String, long[]> intervals = new LinkedHashMap<>();

        Instant now = Instant.now();
        for (int i = 5; i >= 0; i--) {
            Instant t = now.minus(i * 2, ChronoUnit.MINUTES);
            String label = String.format("%02d:%02d", t.atZone(java.time.ZoneId.systemDefault()).getHour(),
                    t.atZone(java.time.ZoneId.systemDefault()).getMinute());
            intervals.put(label, new long[]{0, 0});
        }

        for (RequestAuditLog log : recentLogs) {
            String label = String.format("%02d:%02d",
                    log.getTimestamp().atZone(java.time.ZoneId.systemDefault()).getHour(),
                    log.getTimestamp().atZone(java.time.ZoneId.systemDefault()).getMinute());
            if (intervals.containsKey(label)) {
                if (log.isAllowed()) {
                    intervals.get(label)[0]++;
                } else {
                    intervals.get(label)[1]++;
                }
            }
        }

        return intervals.entrySet().stream()
                .map(e -> new AnalyticsResponses.TrafficTimelinePoint(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .collect(Collectors.toList());
    }

    private List<AnalyticsResponses.EndpointBreakdown> buildEndpointBreakdowns() {
        List<RequestAuditLog> recentLogs = auditLogRepository.findTop50ByOrderByTimestampDesc();
        Map<String, long[]> map = new HashMap<>();

        for (RequestAuditLog log : recentLogs) {
            String key = log.getHttpMethod() + " " + log.getEndpoint();
            map.putIfAbsent(key, new long[]{0, 0});
            map.get(key)[0]++; // total
            if (!log.isAllowed()) {
                map.get(key)[1]++; // blocked
            }
        }

        return map.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split(" ", 2);
                    return new AnalyticsResponses.EndpointBreakdown(parts[1], parts[0], e.getValue()[0], e.getValue()[1]);
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private AnalyticsResponses.LogEntryDto toLogDto(RequestAuditLog l) {
        return new AnalyticsResponses.LogEntryDto(
                l.getId(),
                l.getApplicationId(),
                l.getApiKeyPrefix(),
                l.getEndpoint(),
                l.getHttpMethod(),
                l.getHttpStatus(),
                l.isAllowed(),
                l.getClientIp(),
                l.getLatencyMs(),
                l.getRemainingTokens(),
                l.getTimestamp().toString()
        );
    }
}
