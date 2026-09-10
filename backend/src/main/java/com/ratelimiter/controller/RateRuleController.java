package com.ratelimiter.controller;

import com.ratelimiter.dto.RuleRequests;
import com.ratelimiter.model.RateLimitRule;
import com.ratelimiter.repository.RateLimitRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Rate Limit Rules", description = "Configuration of Token Bucket capacity and refill policies")
@CrossOrigin(origins = "*")
public class RateRuleController {

    private final RateLimitRuleRepository ruleRepository;

    public RateRuleController(RateLimitRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    @Operation(summary = "List all rate limit rules")
    public ResponseEntity<List<RuleRequests.RateLimitRuleDto>> getRules(@RequestParam(required = false) Long appId) {
        List<RateLimitRule> rules;
        if (appId != null) {
            rules = ruleRepository.findByApplicationId(appId);
        } else {
            rules = ruleRepository.findAll();
        }

        List<RuleRequests.RateLimitRuleDto> dtos = rules.stream().map(r ->
                new RuleRequests.RateLimitRuleDto(
                        r.getId(),
                        r.getApplicationId(),
                        r.getPlanTier(),
                        r.getEndpointPattern(),
                        r.getHttpMethod(),
                        r.getCapacity(),
                        r.getRefillTokens(),
                        r.getRefillPeriodSeconds(),
                        r.isActive(),
                        r.getDescription(),
                        r.getCreatedAt().toString()
                )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Create or customize a rate limit rule")
    public ResponseEntity<RuleRequests.RateLimitRuleDto> createRule(@Valid @RequestBody RuleRequests.CreateRuleRequest request) {
        RateLimitRule rule = new RateLimitRule(
                request.getApplicationId(),
                request.getPlanTier(),
                request.getEndpointPattern(),
                request.getHttpMethod(),
                request.getCapacity(),
                request.getRefillTokens(),
                request.getRefillPeriodSeconds(),
                request.getDescription()
        );
        rule = ruleRepository.save(rule);

        return ResponseEntity.ok(new RuleRequests.RateLimitRuleDto(
                rule.getId(),
                rule.getApplicationId(),
                rule.getPlanTier(),
                rule.getEndpointPattern(),
                rule.getHttpMethod(),
                rule.getCapacity(),
                rule.getRefillTokens(),
                rule.getRefillPeriodSeconds(),
                rule.isActive(),
                rule.getDescription(),
                rule.getCreatedAt().toString()
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rate limit rule")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
