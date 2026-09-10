package com.ratelimiter.controller;

import com.ratelimiter.dto.AppRequests;
import com.ratelimiter.security.UserPrincipal;
import com.ratelimiter.service.app.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apps")
@Tag(name = "Applications & Keys", description = "Application registration and API key management")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "List all applications owned by the logged-in developer")
    public ResponseEntity<List<AppRequests.ApplicationDto>> getApplications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(applicationService.getApplicationsForUser(principal.getId()));
    }

    @PostMapping
    @Operation(summary = "Create a new application with initial API key")
    public ResponseEntity<AppRequests.ApplicationDto> createApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AppRequests.CreateAppRequest request) {
        return ResponseEntity.ok(applicationService.createApplication(principal.getId(), request));
    }

    @PostMapping("/{appId}/keys")
    @Operation(summary = "Generate a new API key for an application")
    public ResponseEntity<AppRequests.ApiKeyDto> generateApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long appId,
            @RequestBody(required = false) AppRequests.GenerateKeyRequest request) {
        String name = request != null ? request.getKeyName() : "API Key";
        return ResponseEntity.ok(applicationService.generateApiKey(principal.getId(), appId, name));
    }

    @PatchMapping("/keys/{keyId}/toggle")
    @Operation(summary = "Toggle active/inactive status of an API key")
    public ResponseEntity<Void> toggleApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long keyId) {
        applicationService.toggleApiKey(principal.getId(), keyId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{appId}")
    @Operation(summary = "Delete an application and revoke all its keys")
    public ResponseEntity<Void> deleteApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long appId) {
        applicationService.deleteApplication(principal.getId(), appId);
        return ResponseEntity.noContent().build();
    }
}
