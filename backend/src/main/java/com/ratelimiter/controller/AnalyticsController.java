package com.ratelimiter.controller;

import com.ratelimiter.dto.AnalyticsResponses;
import com.ratelimiter.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Telemetry", description = "Real-time traffic metrics, status counters, and audit logs")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overview metrics: total requests, allowed, 429 blocked, latency, and top endpoints")
    public ResponseEntity<AnalyticsResponses.DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/logs")
    @Operation(summary = "Get paginated historical request audit logs")
    public ResponseEntity<Page<AnalyticsResponses.LogEntryDto>> getLogs(
            @RequestParam(required = false) Long appId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getAuditLogs(appId, page, size));
    }
}
