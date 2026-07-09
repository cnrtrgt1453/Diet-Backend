package com.diet.backend.controller;

import com.diet.backend.dto.ProfileUpdateRequest;
import com.diet.backend.model.Measurement;
import com.diet.backend.model.User;
import com.diet.backend.repository.MeasurementRepository;
import com.diet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User currentUser, @RequestBody ProfileUpdateRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        boolean isFirstWeight = user.getCurrentWeight() == null;

        user.setHeight(request.getHeight());
        user.setCurrentWeight(request.getCurrentWeight());
        user.setTargetWeight(request.getTargetWeight());
        user.setCategory(request.getCategory());
        user.setGlp1InjectionDay(request.getGlp1InjectionDay());
        user.setGlp1Dosage(request.getGlp1Dosage());
        user.setLipedemaStage(request.getLipedemaStage());
        user.setAntiInflammatoryCompliant(request.getAntiInflammatoryCompliant());
        user.setHormoneTargetCycle(request.getHormoneTargetCycle());

        User savedUser = userRepository.save(user);

        // Eğer ilk kez ağırlık ekleniyorsa, ölçüm geçmişine kaydet
        if (isFirstWeight && savedUser.getCurrentWeight() != null) {
            measurementRepository.save(Measurement.builder()
                    .client(savedUser)
                    .date(LocalDate.now())
                    .weight(savedUser.getCurrentWeight())
                    .note("İlk profil kurulum ölçümü.")
                    .build());
        }

        return ResponseEntity.ok(savedUser);
    }
}
