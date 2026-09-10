package com.ratelimiter.service.ratelimit;

import com.ratelimiter.model.ApiKey;
import com.ratelimiter.model.Application;
import com.ratelimiter.model.RateLimitRule;
import com.ratelimiter.model.RequestAuditLog;
import com.ratelimiter.model.enums.PlanTier;
import com.ratelimiter.repository.ApiKeyRepository;
import com.ratelimiter.repository.ApplicationRepository;
import com.ratelimiter.repository.RateLimitRuleRepository;
import com.ratelimiter.repository.RequestAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final RedisTokenBucketRateLimiter redisRateLimiter;
    private final InMemoryTokenBucketRateLimiter inMemoryRateLimiter;
    private final ApiKeyRepository apiKeyRepository;
    private final ApplicationRepository applicationRepository;
    private final RateLimitRuleRepository ruleRepository;
    private final RequestAuditLogRepository auditLogRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${app.rate-limiter.default-capacity:60}")
    private int defaultCapacity;

    @Value("${app.rate-limiter.default-refill-tokens:60}")
    private int defaultRefillTokens;

    @Value("${app.rate-limiter.default-refill-period-seconds:60}")
    private int defaultRefillPeriodSeconds;

    public RateLimiterService(RedisTokenBucketRateLimiter redisRateLimiter,
                              InMemoryTokenBucketRateLimiter inMemoryRateLimiter,
                              ApiKeyRepository apiKeyRepository,
                              ApplicationRepository applicationRepository,
                              RateLimitRuleRepository ruleRepository,
                              RequestAuditLogRepository auditLogRepository) {
        this.redisRateLimiter = redisRateLimiter;
        this.inMemoryRateLimiter = inMemoryRateLimiter;
        this.apiKeyRepository = apiKeyRepository;
        this.applicationRepository = applicationRepository;
        this.ruleRepository = ruleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Evaluates a rate limit check for an incoming request.
     */
    public RateLimitDecision evaluate(String apiKeyHeader, String clientIp, String endpoint, String httpMethod) {
        long startTime = System.currentTimeMillis();

        // 1. Identify client
        RateLimitKey key;
        ResolvedRule rule;
        Long applicationId = null;
        String apiKeyPrefix = null;

        if (apiKeyHeader != null && !apiKeyHeader.trim().isEmpty()) {
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findActiveKeyWithApplication(apiKeyHeader.trim());
            if (apiKeyOpt.isPresent()) {
                ApiKey apiKey = apiKeyOpt.get();
                Application app = apiKey.getApplication();
                applicationId = app.getId();
                apiKeyPrefix = apiKey.getKeyPrefix();
                key = new RateLimitKey("API_KEY", apiKey.getKeyValue(), endpoint);
                rule = resolveRuleForApp(app, endpoint, httpMethod);
            } else {
                // Invalid or inactive API key - fallback to IP-based rate limiting with strict defaults
                key = new RateLimitKey("IP", clientIp != null ? clientIp : "unknown", endpoint);
                rule = resolveDefaultRule(endpoint, httpMethod, PlanTier.FREE);
            }
        } else {
            // Client identified by IP
            key = new RateLimitKey("IP", clientIp != null ? clientIp : "unknown", endpoint);
            rule = resolveDefaultRule(endpoint, httpMethod, PlanTier.FREE);
        }

        // 2. Execute rate limit check with fallback support
        RateLimitResult result;
        try {
            result = redisRateLimiter.tryAcquire(key, rule.capacity, rule.refillTokens, rule.refillPeriodSeconds, 1);
        } catch (Exception ex) {
            // Failover to In-Memory
            result = inMemoryRateLimiter.tryAcquire(key, rule.capacity, rule.refillTokens, rule.refillPeriodSeconds, 1);
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        return new RateLimitDecision(result, applicationId, apiKeyPrefix, key, rule, latencyMs);
    }

    /**
     * Records telemetry log for the completed request.
     */
    public void recordLog(RateLimitDecision decision, String endpoint, String httpMethod, int httpStatus, String clientIp) {
        try {
            RequestAuditLog auditLog = new RequestAuditLog(
                    decision.getApplicationId(),
                    decision.getApiKeyPrefix(),
                    endpoint,
                    httpMethod,
                    httpStatus,
                    decision.getResult().isAllowed(),
                    clientIp,
                    decision.getLatencyMs(),
                    decision.getResult().getRemaining()
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to record request audit log: {}", e.getMessage());
        }
    }

    private ResolvedRule resolveRuleForApp(Application app, String endpoint, String httpMethod) {
        List<RateLimitRule> rules = ruleRepository.findMatchingRulesForApp(app.getId());

        // Check for specific endpoint and method match
        for (RateLimitRule r : rules) {
            if (matchesEndpoint(r.getEndpointPattern(), endpoint) && matchesMethod(r.getHttpMethod(), httpMethod)) {
                return new ResolvedRule(r.getCapacity(), r.getRefillTokens(), r.getRefillPeriodSeconds());
            }
        }

        // Fallback to Plan Tier defaults
        PlanTier tier = app.getPlanTier();
        return new ResolvedRule(tier.getDefaultCapacity(), tier.getDefaultRefillTokens(), tier.getDefaultRefillPeriodSeconds());
    }

    private ResolvedRule resolveDefaultRule(String endpoint, String httpMethod, PlanTier tier) {
        List<RateLimitRule> rules = ruleRepository.findByActiveTrue();
        for (RateLimitRule r : rules) {
            if (r.getApplicationId() == null && matchesEndpoint(r.getEndpointPattern(), endpoint) && matchesMethod(r.getHttpMethod(), httpMethod)) {
                return new ResolvedRule(r.getCapacity(), r.getRefillTokens(), r.getRefillPeriodSeconds());
            }
        }
        return new ResolvedRule(tier.getDefaultCapacity(), tier.getDefaultRefillTokens(), tier.getDefaultRefillPeriodSeconds());
    }

    private boolean matchesEndpoint(String pattern, String path) {
        if (pattern == null || pattern.equals("/**") || pattern.equals("*")) {
            return true;
        }
        return pathMatcher.match(pattern, path);
    }

    private boolean matchesMethod(String ruleMethod, String requestMethod) {
        if (ruleMethod == null || ruleMethod.equalsIgnoreCase("ALL")) {
            return true;
        }
        return ruleMethod.equalsIgnoreCase(requestMethod);
    }

    public static class ResolvedRule {
        public final int capacity;
        public final int refillTokens;
        public final int refillPeriodSeconds;

        public ResolvedRule(int capacity, int refillTokens, int refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriodSeconds = refillPeriodSeconds;
        }
    }

    public static class RateLimitDecision {
        private final RateLimitResult result;
        private final Long applicationId;
        private final String apiKeyPrefix;
        private final RateLimitKey key;
        private final ResolvedRule rule;
        private final long latencyMs;

        public RateLimitDecision(RateLimitResult result, Long applicationId, String apiKeyPrefix,
                                 RateLimitKey key, ResolvedRule rule, long latencyMs) {
            this.result = result;
            this.applicationId = applicationId;
            this.apiKeyPrefix = apiKeyPrefix;
            this.key = key;
            this.rule = rule;
            this.latencyMs = latencyMs;
        }

        public RateLimitResult getResult() {
            return result;
        }

        public Long getApplicationId() {
            return applicationId;
        }

        public String getApiKeyPrefix() {
            return apiKeyPrefix;
        }

        public RateLimitKey getKey() {
            return key;
        }

        public ResolvedRule getRule() {
            return rule;
        }

        public long getLatencyMs() {
            return latencyMs;
        }
    }
}
