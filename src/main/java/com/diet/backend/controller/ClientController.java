package com.diet.backend.controller;

import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.*;
import com.diet.backend.service.ClientAnalyticsService;
import com.diet.backend.service.ClientService;
import com.diet.backend.service.DietPlanService;
import com.diet.backend.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final MeasurementService measurementService;
    private final DietPlanService dietPlanService;
    private final ClientAnalyticsService clientAnalyticsService;

    // Giriş yapan diyetisyenin tüm danışanlarını getirir
    @GetMapping
    public ResponseEntity<List<User>> getClients() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> clients = clientService.getClients(dietitian);
        return ResponseEntity.ok(clients);
    }

    // Diyetisyen istatistiklerini getirir
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> stats = clientAnalyticsService.getStats(dietitian);
        return ResponseEntity.ok(stats);
    }

    // Yeni danışan ekler
    @PostMapping
    public ResponseEntity<?> addClient(@RequestBody User clientRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User savedClient = clientService.addClient(clientRequest, dietitian);
        return ResponseEntity.ok(savedClient);
    }

    // Danışan günceller
    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(@PathVariable Long clientId, @RequestBody User clientRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User savedClient = clientService.updateClient(clientId, clientRequest, dietitian);
        return ResponseEntity.ok(savedClient);
    }

    // Danışan siler
    @DeleteMapping("/{clientId}")
    public ResponseEntity<?> deleteClient(@PathVariable Long clientId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        clientService.deleteClient(clientId, dietitian);
        return ResponseEntity.ok().build();
    }

    // Danışanın ölçüm geçmişini getirir
    @GetMapping("/{clientId}/measurements")
    public ResponseEntity<?> getClientMeasurements(@PathVariable Long clientId) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Measurement> measurements = measurementService.getClientMeasurements(clientId, loggedInUser);
        return ResponseEntity.ok(measurements);
    }

    // Danışana yeni ölçüm ekler
    @PostMapping("/{clientId}/measurements")
    public ResponseEntity<?> addMeasurement(@PathVariable Long clientId, @RequestBody Measurement measurementRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Measurement saved = measurementService.addMeasurement(clientId, measurementRequest, dietitian);
        return ResponseEntity.ok(saved);
    }

    // Danışanın diyet planlarını listeler
    @GetMapping("/{clientId}/diets")
    public ResponseEntity<?> getClientDiets(@PathVariable Long clientId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DietPlan> dietPlans = dietPlanService.getClientDiets(clientId, dietitian);
        return ResponseEntity.ok(dietPlans);
    }

    // Danışana yeni diyet planı ekler
    @PostMapping("/{clientId}/diets")
    public ResponseEntity<?> addDietPlan(@PathVariable Long clientId, @RequestBody DietPlan dietPlanRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DietPlan saved = dietPlanService.addDietPlan(clientId, dietPlanRequest, dietitian);
        return ResponseEntity.ok(saved);
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
