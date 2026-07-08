package com.diet.backend.repository;

import com.diet.backend.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByClientIdOrderByDateDesc(Long clientId);
    Optional<DietPlan> findByClientIdAndDate(Long clientId, LocalDate date);
    List<DietPlan> findByClientIdAndDateBetweenOrderByDateAsc(Long clientId, LocalDate startDate, LocalDate endDate);
}
