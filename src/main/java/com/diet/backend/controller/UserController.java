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

        if (user.getRole() == com.diet.backend.model.Role.ROLE_DIETITIAN) {
            if (request.getName() != null) user.setName(request.getName());
            user.setInstagramUrl(request.getInstagramUrl());
            user.setLinkedinUrl(request.getLinkedinUrl());
            user.setYoutubeUrl(request.getYoutubeUrl());
            user.setProfilePictureUrl(request.getProfilePictureUrl());
            if (request.getNotes() != null) user.setNotes(request.getNotes());
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        }

        boolean isFirstWeight = user.getCurrentWeight() == null;

        if (request.getName() != null) user.setName(request.getName());
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

    @PostMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @AuthenticationPrincipal User currentUser,
            @RequestBody java.util.Map<String, String> request
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        String fcmToken = request.get("fcmToken");
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
