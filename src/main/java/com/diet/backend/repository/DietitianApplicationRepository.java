package com.diet.backend.repository;

import com.diet.backend.model.DietitianApplication;
import com.diet.backend.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DietitianApplicationRepository extends JpaRepository<DietitianApplication, Long> {
    Optional<DietitianApplication> findByEmail(String email);
    Optional<DietitianApplication> findByUserId(Long userId);
    List<DietitianApplication> findByStatus(ApplicationStatus status);
}
