package com.ratelimiter.config;

import com.ratelimiter.model.*;
import com.ratelimiter.model.enums.PlanTier;
import com.ratelimiter.model.enums.Role;
import com.ratelimiter.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DeveloperUserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final RateLimitRuleRepository ruleRepository;
    private final RequestAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DeveloperUserRepository userRepository,
                           ApplicationRepository applicationRepository,
                           ApiKeyRepository apiKeyRepository,
                           RateLimitRuleRepository ruleRepository,
                           RequestAuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.ruleRepository = ruleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Initializing demo dataset for Distributed Rate Limiter...");

        // 1. Create Demo Developer
        DeveloperUser developer = new DeveloperUser(
                "developer@ratelimiter.io",
                "Alex Rivera (Principal Architect)",
                passwordEncoder.encode("password123"),
                Role.ROLE_DEVELOPER
        );
        developer = userRepository.save(developer);

        // 2. Create Sample Applications with API Keys
        Application freeApp = new Application("E-Commerce Web Store", "Main online shopping storefront application", PlanTier.FREE, developer.getId());
        freeApp = applicationRepository.save(freeApp);
        ApiKey freeKey = new ApiKey("rl_live_ecommerce_demo_key_771", "rl_live_ecom...", "Production Store Key", freeApp);
        apiKeyRepository.save(freeKey);

        Application proApp = new Application("Mobile iOS / Android App", "Native mobile application client", PlanTier.PRO, developer.getId());
        proApp = applicationRepository.save(proApp);
        ApiKey proKey = new ApiKey("rl_live_mobile_app_demo_key_882", "rl_live_mobi...", "Mobile App Key", proApp);
        apiKeyRepository.save(proKey);

        Application enterpriseApp = new Application("Partner Logistics Integration", "High-frequency B2B partner synchronization", PlanTier.ENTERPRISE, developer.getId());
        enterpriseApp = applicationRepository.save(enterpriseApp);
        ApiKey enterpriseKey = new ApiKey("rl_live_enterprise_demo_key_993", "rl_live_ente...", "B2B Partner Key", enterpriseApp);
        apiKeyRepository.save(enterpriseKey);

        // 3. Create Custom Rate Rules
        RateLimitRule orderRule = new RateLimitRule(
                freeApp.getId(),
                PlanTier.FREE,
                "/api/v1/gateway/orders",
                "POST",
                5, 5, 60,
                "Strict order placement throttle to prevent duplicate carts"
        );
        ruleRepository.save(orderRule);

        RateLimitRule searchRule = new RateLimitRule(
                null, // Global default rule
                null,
                "/api/v1/gateway/search",
                "GET",
                25, 25, 60,
                "Search query burst limiter across all tiers"
        );
        ruleRepository.save(searchRule);

        // 4. Seed initial realistic telemetry audit logs
        Random random = new Random();
        Instant now = Instant.now();
        String[] endpoints = {"/api/v1/gateway/products", "/api/v1/gateway/orders", "/api/v1/gateway/search", "/api/v1/gateway/status"};
        String[] methods = {"GET", "POST", "GET", "GET"};
        String[] ips = {"192.168.1.101", "10.0.4.22", "172.16.0.45", "198.51.100.78", "203.0.113.12"};

        for (int i = 0; i < 40; i++) {
            int epIndex = random.nextInt(endpoints.length);
            boolean isAllowed = (i % 7 != 0); // ~15% rejection rate for simulation
            int status = isAllowed ? (methods[epIndex].equals("POST") ? 201 : 200) : 429;
            long latency = isAllowed ? (random.nextInt(35) + 5) : (random.nextInt(5) + 1);
            int remaining = isAllowed ? (random.nextInt(15) + 1) : 0;
            Instant logTime = now.minus(random.nextInt(90), ChronoUnit.MINUTES);

            RequestAuditLog logEntry = new RequestAuditLog(
                    freeApp.getId(),
                    freeKey.getKeyPrefix(),
                    endpoints[epIndex],
                    methods[epIndex],
                    status,
                    isAllowed,
                    ips[random.nextInt(ips.length)],
                    latency,
                    remaining
            );
            logEntry.setTimestamp(logTime);
            auditLogRepository.save(logEntry);
        }

        log.info("Initialized demo dataset successfully!");
    }
}
