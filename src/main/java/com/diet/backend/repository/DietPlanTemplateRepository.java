package com.diet.backend.repository;

import com.diet.backend.model.DietPlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietPlanTemplateRepository extends JpaRepository<DietPlanTemplate, Long> {
    // Diyetisyene göre şablonları bulmak için
    List<DietPlanTemplate> findByDietitianId(Long dietitianId);
}
