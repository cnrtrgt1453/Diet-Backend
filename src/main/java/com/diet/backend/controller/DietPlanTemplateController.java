package com.diet.backend.controller;

import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.DietPlan;
import com.diet.backend.model.DietPlanTemplate;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.service.DietPlanTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diet-templates")
@RequiredArgsConstructor
public class DietPlanTemplateController {

    private final DietPlanTemplateService templateService;

    // Diyetisyen: Yeni diyet şablonu oluşturur
    @PostMapping
    public ResponseEntity<?> saveTemplate(@RequestBody DietPlanTemplate templateRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Sadece diyetisyenler şablon oluşturabilir.");
        }
        
        DietPlanTemplate saved = templateService.saveTemplate(templateRequest, dietitian);
        return ResponseEntity.ok(saved);
    }

    // Diyetisyen: Mevcut bir diyet planından şablon türetir
    @PostMapping("/from-plan/{planId}")
    public ResponseEntity<?> createTemplateFromPlan(
            @PathVariable Long planId, 
            @RequestParam(required = false) String title) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Sadece diyetisyenler şablon oluşturabilir.");
        }

        DietPlanTemplate saved = templateService.createTemplateFromPlan(planId, title, dietitian);
        return ResponseEntity.ok(saved);
    }

    // Diyetisyen: Kendi şablonlarını listeler
    @GetMapping
    public ResponseEntity<List<DietPlanTemplate>> getTemplates() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DietPlanTemplate> templates = templateService.getTemplates(dietitian);
        return ResponseEntity.ok(templates);
    }

    // Diyetisyen: Şablonu bir danışana diyet planı olarak atar
    @PostMapping("/{templateId}/assign/{clientId}")
    public ResponseEntity<?> assignTemplate(
            @PathVariable Long templateId, 
            @PathVariable Long clientId, 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Sadece diyetisyenler şablon ataması yapabilir.");
        }

        DietPlan assigned = templateService.assignTemplate(templateId, clientId, date, dietitian);
        return ResponseEntity.ok(assigned);
    }

    // Diyetisyen: Şablonu siler
    @DeleteMapping("/{templateId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long templateId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Sadece diyetisyenler şablon silebilir.");
        }

        templateService.deleteTemplate(templateId, dietitian);
        return ResponseEntity.ok().build();
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
