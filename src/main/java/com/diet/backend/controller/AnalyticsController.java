package com.diet.backend.controller;

import com.diet.backend.dto.*;
import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.User;
import com.diet.backend.service.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AdvancedAnalyticsService advancedAnalyticsService;

    // Diyetisyene bağlı danışanların kohort analizini getirir
    @GetMapping("/dietitian/cohorts")
    public ResponseEntity<List<CohortDto>> getCohortAnalysis() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<CohortDto> cohortAnalysis = advancedAnalyticsService.getCohortAnalysis(dietitian);
        return ResponseEntity.ok(cohortAnalysis);
    }

    // Diyetisyene bağlı danışanların kategori bazlı uyum oranlarını getirir
    @GetMapping("/dietitian/compliance")
    public ResponseEntity<List<CategoryComplianceDto>> getCategoryCompliance() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<CategoryComplianceDto> compliance = advancedAnalyticsService.getCategoryCompliance(dietitian);
        return ResponseEntity.ok(compliance);
    }

    // Diyetisyene bağlı danışanların kilo kaybı hızlarını getirir
    @GetMapping("/dietitian/rates")
    public ResponseEntity<List<ClientWeightLossRateDto>> getClientWeightLossRates() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ClientWeightLossRateDto> rates = advancedAnalyticsService.getClientWeightLossRates(dietitian);
        return ResponseEntity.ok(rates);
    }

    // Danışanın uyum korelasyon verilerini getirir
    @GetMapping("/client/{clientId}/correlation")
    public ResponseEntity<CorrelationAnalysisDto> getCorrelationAnalysis(@PathVariable Long clientId) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CorrelationAnalysisDto correlationAnalysis = advancedAnalyticsService.getCorrelationAnalysis(clientId, loggedInUser);
        return ResponseEntity.ok(correlationAnalysis);
    }

    // Danışanın kilo hedefi tahmin verilerini getirir
    @GetMapping("/client/{clientId}/prediction")
    public ResponseEntity<WeightPredictionDto> getWeightPrediction(@PathVariable Long clientId) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WeightPredictionDto prediction = advancedAnalyticsService.getWeightPrediction(clientId, loggedInUser);
        return ResponseEntity.ok(prediction);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }
}
