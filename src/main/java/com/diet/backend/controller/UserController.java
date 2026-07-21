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
            user.setXUrl(request.getXUrl());
            user.setFacebookUrl(request.getFacebookUrl());
            user.setProfilePictureUrl(request.getProfilePictureUrl());
            if (request.getNotes() != null) user.setNotes(request.getNotes());
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        }

        boolean isFirstWeight = user.getCurrentWeight() == null;

        if (request.getName() != null) user.setName(request.getName());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
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

    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "user_" + currentUser.getId() + "_" + System.currentTimeMillis() + extension;
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://10.0.2.2:8080/api/v1/users/profile-picture/" + filename;
            
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            user.setProfilePictureUrl(fileUrl);
            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Dosya yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/profile-picture/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getProfilePicture(@PathVariable String filename) {
        try {
            java.nio.file.Path file = java.nio.file.Paths.get("uploads").resolve(filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "image/jpeg")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
