package com.ratelimiter.service.app;

import com.ratelimiter.dto.AppRequests;
import com.ratelimiter.model.ApiKey;
import com.ratelimiter.model.Application;
import com.ratelimiter.repository.ApiKeyRepository;
import com.ratelimiter.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApplicationService(ApplicationRepository applicationRepository, ApiKeyRepository apiKeyRepository) {
        this.applicationRepository = applicationRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional(readOnly = true)
    public List<AppRequests.ApplicationDto> getApplicationsForUser(Long userId) {
        return applicationRepository.findByDeveloperId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppRequests.ApplicationDto createApplication(Long userId, AppRequests.CreateAppRequest request) {
        Application app = new Application(
                request.getName(),
                request.getDescription(),
                request.getPlanTier(),
                userId
        );
        app = applicationRepository.save(app);

        // Auto-generate primary API key
        ApiKey defaultKey = generateApiKeyEntity(app, "Default Key");
        apiKeyRepository.save(defaultKey);
        app.getApiKeys().add(defaultKey);

        return toDto(app);
    }

    @Transactional
    public AppRequests.ApiKeyDto generateApiKey(Long userId, Long appId, String keyName) {
        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getDeveloperId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized application access");
        }

        ApiKey apiKey = generateApiKeyEntity(app, keyName != null ? keyName : "API Key");
        apiKey = apiKeyRepository.save(apiKey);

        return toKeyDto(apiKey, true);
    }

    @Transactional
    public void toggleApiKey(Long userId, Long keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found"));

        if (!apiKey.getApplication().getDeveloperId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized API key access");
        }

        apiKey.setActive(!apiKey.isActive());
        apiKeyRepository.save(apiKey);
    }

    @Transactional
    public void deleteApplication(Long userId, Long appId) {
        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getDeveloperId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized application access");
        }

        applicationRepository.delete(app);
    }

    private ApiKey generateApiKeyEntity(Application app, String name) {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String rawKey = "rl_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String prefix = rawKey.substring(0, 12) + "...";

        return new ApiKey(rawKey, prefix, name, app);
    }

    public AppRequests.ApplicationDto toDto(Application app) {
        List<AppRequests.ApiKeyDto> keyDtos = app.getApiKeys().stream()
                .map(k -> toKeyDto(k, false))
                .collect(Collectors.toList());

        return new AppRequests.ApplicationDto(
                app.getId(),
                app.getName(),
                app.getDescription(),
                app.getPlanTier(),
                app.isActive(),
                app.getCreatedAt().toString(),
                keyDtos,
                app.getPlanTier().getDefaultCapacity(),
                app.getPlanTier().getDefaultRefillTokens(),
                app.getPlanTier().getDefaultRefillPeriodSeconds()
        );
    }

    public AppRequests.ApiKeyDto toKeyDto(ApiKey key, boolean includeFullSecret) {
        return new AppRequests.ApiKeyDto(
                key.getId(),
                includeFullSecret ? key.getKeyValue() : key.getKeyValue(), // In developer dashboard, users can view their created keys
                key.getKeyPrefix(),
                key.getName(),
                key.isActive(),
                key.getCreatedAt().toString()
        );
    }
}
