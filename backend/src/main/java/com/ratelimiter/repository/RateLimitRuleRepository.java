package com.ratelimiter.repository;

import com.ratelimiter.model.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {
    List<RateLimitRule> findByApplicationId(Long applicationId);
    List<RateLimitRule> findByActiveTrue();

    @Query("SELECT r FROM RateLimitRule r WHERE (r.applicationId = :appId OR r.applicationId IS NULL) AND r.active = true ORDER BY r.applicationId DESC")
    List<RateLimitRule> findMatchingRulesForApp(@Param("appId") Long appId);
}
