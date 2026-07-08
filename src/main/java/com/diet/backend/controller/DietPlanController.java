package com.diet.backend.controller;

import com.diet.backend.model.DietPlan;
import com.diet.backend.model.User;
import com.diet.backend.repository.DietPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/diets/my")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanRepository dietPlanRepository;

    // Giriş yapan danışanın kendi diyet planlarını listeler
    @GetMapping
    public ResponseEntity<List<DietPlan>> getMyDiets() {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DietPlan> dietPlans = dietPlanRepository.findByClientIdOrderByDateDesc(client.getId());
        return ResponseEntity.ok(dietPlans);
    }

    // Bugünün diyet planını getirir
    @GetMapping("/today")
    public ResponseEntity<?> getTodayDiet() {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<DietPlan> todayDiet = dietPlanRepository.findByClientIdAndDate(client.getId(), LocalDate.now());
        if (todayDiet.isEmpty()) {
            return ResponseEntity.ok().body("Bugün için planlanmış bir diyet programı bulunmuyor.");
        }
        return ResponseEntity.ok(todayDiet.get());
    }

    // Diyet planını tamamlandı/tamamlanmadı olarak işaretler
    @PostMapping("/{dietId}/toggle")
    public ResponseEntity<?> toggleDietCompleted(@PathVariable Long dietId) {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<DietPlan> dietOpt = dietPlanRepository.findById(dietId);
        if (dietOpt.isEmpty() || !dietOpt.get().getClient().getId().equals(client.getId())) {
            return ResponseEntity.notFound().build();
        }

        DietPlan dietPlan = dietOpt.get();
        dietPlan.setCompleted(!dietPlan.getCompleted());
        DietPlan saved = dietPlanRepository.save(dietPlan);
        
        return ResponseEntity.ok(saved);
    }
}
