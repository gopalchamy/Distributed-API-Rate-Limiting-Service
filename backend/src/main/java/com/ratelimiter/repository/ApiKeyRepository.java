package com.ratelimiter.repository;

import com.ratelimiter.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
    List<ApiKey> findByApplicationId(Long applicationId);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.application WHERE k.keyValue = :keyValue AND k.active = true AND k.application.active = true")
    Optional<ApiKey> findActiveKeyWithApplication(@Param("keyValue") String keyValue);
}
