package com.ratelimiter.repository;

import com.ratelimiter.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByDeveloperId(Long developerId);
    List<Application> findByActiveTrue();
}
