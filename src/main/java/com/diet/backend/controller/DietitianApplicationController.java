package com.diet.backend.controller;

import com.diet.backend.dto.DietitianApplicationDto;
import com.diet.backend.dto.DietitianApplicationReviewDto;
import com.diet.backend.model.DietitianApplication;
import com.diet.backend.model.User;
import com.diet.backend.service.DietitianApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DietitianApplicationController {

    private final DietitianApplicationService applicationService;

    // Herkese açık başvuru endpointi (SecurityConfig tarafından /api/v1/auth/** izin verilir)
    @PostMapping("/auth/apply-dietitian")
    public ResponseEntity<?> apply(@RequestBody DietitianApplicationDto dto) {
        try {
            DietitianApplication application = applicationService.apply(dto);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bekleyen başvuruları listele (Admin Yetkili)
    @GetMapping("/admin/applications")
    public ResponseEntity<?> getPendingApplications(@AuthenticationPrincipal User adminUser) {
        try {
            List<DietitianApplication> applications = applicationService.getPendingApplications(adminUser);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // Başvuru Onayla (Admin Yetkili)
    @PostMapping("/admin/applications/{id}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Long id, @AuthenticationPrincipal User adminUser) {
        try {
            DietitianApplication application = applicationService.approveApplication(id, adminUser);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Başvuru Reddet (Admin Yetkili)
    @PostMapping("/admin/applications/{id}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long id,
            @RequestBody DietitianApplicationReviewDto reviewDto,
            @AuthenticationPrincipal User adminUser) {
        try {
            DietitianApplication application = applicationService.rejectApplication(id, reviewDto.getRejectionReason(), adminUser);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
