package com.ratelimiter.repository;

import com.ratelimiter.model.RequestAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RequestAuditLogRepository extends JpaRepository<RequestAuditLog, Long> {
    Page<RequestAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    Page<RequestAuditLog> findByApplicationIdOrderByTimestampDesc(Long applicationId, Pageable pageable);
    List<RequestAuditLog> findTop50ByOrderByTimestampDesc();

    @Query("SELECT COUNT(l) FROM RequestAuditLog l WHERE l.timestamp >= :since")
    long countTotalRequestsSince(@Param("since") Instant since);

    @Query("SELECT COUNT(l) FROM RequestAuditLog l WHERE l.allowed = true AND l.timestamp >= :since")
    long countAllowedRequestsSince(@Param("since") Instant since);

    @Query("SELECT COUNT(l) FROM RequestAuditLog l WHERE l.allowed = false AND l.timestamp >= :since")
    long countBlockedRequestsSince(@Param("since") Instant since);

    @Query("SELECT AVG(l.latencyMs) FROM RequestAuditLog l WHERE l.timestamp >= :since")
    Double getAverageLatencySince(@Param("since") Instant since);
}
