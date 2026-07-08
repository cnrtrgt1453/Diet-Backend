package com.diet.backend.controller;

import com.diet.backend.model.*;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.DietPlanRepository;
import com.diet.backend.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final UserRepository userRepository;
    private final DietPlanRepository dietPlanRepository;
    private final MeasurementRepository measurementRepository;

    // Giriş yapan diyetisyenin tüm danışanlarını getirir
    @GetMapping
    public ResponseEntity<List<User>> getClients() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> clients = userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
        return ResponseEntity.ok(clients);
    }

    // Diyetisyen istatistiklerini getirir
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", userRepository.countByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER));
        stats.put("glp1", userRepository.countByDietitianIdAndCategory(dietitian.getId(), ClientCategory.GLP_1));
        stats.put("lipedema", userRepository.countByDietitianIdAndCategory(dietitian.getId(), ClientCategory.LIPEDEMA));
        stats.put("weightManagement", userRepository.countByDietitianIdAndCategory(dietitian.getId(), ClientCategory.WEIGHT_MANAGEMENT));
        stats.put("hormonalBalance", userRepository.countByDietitianIdAndCategory(dietitian.getId(), ClientCategory.HORMONAL_BALANCE));
        return ResponseEntity.ok(stats);
    }

    // Yeni danışan ekler
    @PostMapping
    public ResponseEntity<?> addClient(@RequestBody User clientRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (userRepository.existsByEmail(clientRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Bu e-posta adresiyle kayıtlı bir kullanıcı zaten mevcut.");
        }

        User newClient = User.builder()
                .email(clientRequest.getEmail())
                .name(clientRequest.getName())
                .role(Role.ROLE_USER)
                .height(clientRequest.getHeight())
                .currentWeight(clientRequest.getCurrentWeight())
                .targetWeight(clientRequest.getTargetWeight())
                .category(clientRequest.getCategory())
                .notes(clientRequest.getNotes())
                .glp1InjectionDay(clientRequest.getGlp1InjectionDay())
                .glp1Dosage(clientRequest.getGlp1Dosage())
                .lipedemaStage(clientRequest.getLipedemaStage())
                .antiInflammatoryCompliant(clientRequest.getAntiInflammatoryCompliant())
                .hormoneTargetCycle(clientRequest.getHormoneTargetCycle())
                .dietitian(dietitian)
                .build();

        User savedClient = userRepository.save(newClient);
        
        // İlk ölçüm kaydını oluştur
        if (savedClient.getCurrentWeight() != null) {
            measurementRepository.save(Measurement.builder()
                    .client(savedClient)
                    .date(LocalDate.now())
                    .weight(savedClient.getCurrentWeight())
                    .note("İlk kayıt ölçümü.")
                    .build());
        }

        return ResponseEntity.ok(savedClient);
    }

    // Danışan günceller
    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(@PathVariable Long clientId, @RequestBody User clientRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        User existingClient = clientOpt.get();
        existingClient.setName(clientRequest.getName());
        existingClient.setHeight(clientRequest.getHeight());
        existingClient.setTargetWeight(clientRequest.getTargetWeight());
        existingClient.setCategory(clientRequest.getCategory());
        existingClient.setNotes(clientRequest.getNotes());
        existingClient.setGlp1InjectionDay(clientRequest.getGlp1InjectionDay());
        existingClient.setGlp1Dosage(clientRequest.getGlp1Dosage());
        existingClient.setLipedemaStage(clientRequest.getLipedemaStage());
        existingClient.setAntiInflammatoryCompliant(clientRequest.getAntiInflammatoryCompliant());
        existingClient.setHormoneTargetCycle(clientRequest.getHormoneTargetCycle());
        
        // Eğer ağırlık değiştiyse, ölçüm geçmişine yeni seans kaydı olarak ekleyebiliriz
        if (clientRequest.getCurrentWeight() != null && !clientRequest.getCurrentWeight().equals(existingClient.getCurrentWeight())) {
            existingClient.setCurrentWeight(clientRequest.getCurrentWeight());
            measurementRepository.save(Measurement.builder()
                    .client(existingClient)
                    .date(LocalDate.now())
                    .weight(clientRequest.getCurrentWeight())
                    .note("Güncelleme ölçümü.")
                    .build());
        }

        User savedClient = userRepository.save(existingClient);
        return ResponseEntity.ok(savedClient);
    }

    // Danışan siler
    @DeleteMapping("/{clientId}")
    public ResponseEntity<?> deleteClient(@PathVariable Long clientId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        // Önce bağlı ölçümleri ve diyetleri sil
        List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateDesc(clientId);
        measurementRepository.deleteAll(measurements);

        List<DietPlan> dietPlans = dietPlanRepository.findByClientIdOrderByDateDesc(clientId);
        dietPlanRepository.deleteAll(dietPlans);

        userRepository.delete(clientOpt.get());
        return ResponseEntity.ok().build();
    }

    // Danışanın ölçüm geçmişini getirir
    @GetMapping("/{clientId}/measurements")
    public ResponseEntity<?> getClientMeasurements(@PathVariable Long clientId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateDesc(clientId);
        return ResponseEntity.ok(measurements);
    }

    // Danışana yeni ölçüm ekler
    @PostMapping("/{clientId}/measurements")
    public ResponseEntity<?> addMeasurement(@PathVariable Long clientId, @RequestBody Measurement measurementRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        User client = clientOpt.get();
        
        Measurement newMeasurement = Measurement.builder()
                .client(client)
                .date(measurementRequest.getDate() != null ? measurementRequest.getDate() : LocalDate.now())
                .weight(measurementRequest.getWeight())
                .bodyFat(measurementRequest.getBodyFat())
                .muscleMass(measurementRequest.getMuscleMass())
                .note(measurementRequest.getNote())
                .build();

        Measurement saved = measurementRepository.save(newMeasurement);
        
        // Danışanın son kilosunu güncelle
        client.setCurrentWeight(measurementRequest.getWeight());
        userRepository.save(client);

        return ResponseEntity.ok(saved);
    }

    // Danışanın diyet planlarını listeler
    @GetMapping("/{clientId}/diets")
    public ResponseEntity<?> getClientDiets(@PathVariable Long clientId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        List<DietPlan> dietPlans = dietPlanRepository.findByClientIdOrderByDateDesc(clientId);
        return ResponseEntity.ok(dietPlans);
    }

    // Danışana yeni diyet planı ekler
    @PostMapping("/{clientId}/diets")
    public ResponseEntity<?> addDietPlan(@PathVariable Long clientId, @RequestBody DietPlan dietPlanRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.notFound().build();
        }

        User client = clientOpt.get();
        
        DietPlan newDietPlan = DietPlan.builder()
                .client(client)
                .date(dietPlanRequest.getDate() != null ? dietPlanRequest.getDate() : LocalDate.now())
                .title(dietPlanRequest.getTitle())
                .breakfast(dietPlanRequest.getBreakfast())
                .lunch(dietPlanRequest.getLunch())
                .dinner(dietPlanRequest.getDinner())
                .snacks(dietPlanRequest.getSnacks())
                .targetCalories(dietPlanRequest.getTargetCalories())
                .completed(false)
                .build();

        DietPlan saved = dietPlanRepository.save(newDietPlan);
        return ResponseEntity.ok(saved);
    }
}
