package com.ratelimiter.repository;

import com.ratelimiter.model.DeveloperUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeveloperUserRepository extends JpaRepository<DeveloperUser, Long> {
    Optional<DeveloperUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
