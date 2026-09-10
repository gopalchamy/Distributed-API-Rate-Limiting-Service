package com.ratelimiter.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratelimiter.service.ratelimit.RateLimitResult;
import com.ratelimiter.service.ratelimit.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_DECISION_ATTR = "RATE_LIMIT_DECISION";
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitingInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS pre-flight requests to pass through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String apiKey = extractApiKey(request);
        String clientIp = getClientIp(request);

        RateLimiterService.RateLimitDecision decision = rateLimiterService.evaluate(apiKey, clientIp, uri, method);
        RateLimitResult result = decision.getResult();

        // Standard Rate-Limiting Headers (IETF RFC standard draft)
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTimeSeconds()));
        response.setHeader("X-RateLimit-Engine", result.getBackendType());

        if (!result.isAllowed()) {
            response.setStatus(429); // 429 Too Many Requests
            response.setHeader("Retry-After", String.valueOf(result.getRetryAfterSeconds()));
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("status", 429);
            errorBody.put("error", "Too Many Requests");
            errorBody.put("message", "API rate limit exceeded. Please throttle your requests or upgrade your plan.");
            errorBody.put("retryAfterSeconds", result.getRetryAfterSeconds());
            errorBody.put("limit", result.getLimit());
            errorBody.put("remaining", result.getRemaining());
            errorBody.put("engine", result.getBackendType());
            errorBody.put("timestamp", System.currentTimeMillis());

            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(errorBody));
            writer.flush();

            // Record telemetry for the rejected request
            rateLimiterService.recordLog(decision, uri, method, 429, clientIp);
            return false;
        }

        // Store decision in request attribute for recording successful response telemetry later
        request.setAttribute(RATE_LIMIT_DECISION_ATTR, decision);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object attr = request.getAttribute(RATE_LIMIT_DECISION_ATTR);
        if (attr instanceof RateLimiterService.RateLimitDecision) {
            RateLimiterService.RateLimitDecision decision = (RateLimiterService.RateLimitDecision) attr;
            String uri = request.getRequestURI();
            String method = request.getMethod();
            int status = response.getStatus();
            String clientIp = getClientIp(request);

            rateLimiterService.recordLog(decision, uri, method, status, clientIp);
        }
    }

    private String extractApiKey(HttpServletRequest request) {
        String key = request.getHeader("X-API-KEY");
        if (key == null || key.trim().isEmpty()) {
            key = request.getHeader("API-Key");
        }
        if (key == null || key.trim().isEmpty()) {
            key = request.getParameter("apiKey");
        }
        return key;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.trim();
            }
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
