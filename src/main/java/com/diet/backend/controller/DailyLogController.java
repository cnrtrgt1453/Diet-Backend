package com.diet.backend.controller;

import com.diet.backend.event.DailyLogSubmittedEvent;
import com.diet.backend.model.DailyLog;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.repository.DailyLogRepository;
import com.diet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/logs/daily")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 1. Danışan: Günlük durumunu kaydeder (Varsa günceller, yoksa ekler)
    @PostMapping
    public ResponseEntity<?> saveDailyLog(@RequestBody DailyLog logRequest) {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (client.getRole() != Role.ROLE_USER) {
            return ResponseEntity.badRequest().body("Sadece danışanlar günlük durum kaydı girebilir.");
        }

        LocalDate logDate = logRequest.getLogDate() != null ? logRequest.getLogDate() : LocalDate.now();
        Optional<DailyLog> existingLogOpt = dailyLogRepository.findByClientIdAndLogDate(client.getId(), logDate);
        
        DailyLog dailyLog;
        if (existingLogOpt.isPresent()) {
            dailyLog = existingLogOpt.get();
            dailyLog.setWaterIntakeMl(logRequest.getWaterIntakeMl());
            dailyLog.setGlp1SideEffectLevel(logRequest.getGlp1SideEffectLevel());
            dailyLog.setGlp1SideEffects(logRequest.getGlp1SideEffects());
            dailyLog.setLipedemaPainLevel(logRequest.getLipedemaPainLevel());
            dailyLog.setGlutenFreeCompliant(logRequest.getGlutenFreeCompliant());
            dailyLog.setSugarFreeCompliant(logRequest.getSugarFreeCompliant());
            dailyLog.setDairyFreeCompliant(logRequest.getDairyFreeCompliant());
            dailyLog.setCurrentHormonalPhase(logRequest.getCurrentHormonalPhase());
        } else {
            dailyLog = DailyLog.builder()
                    .client(client)
                    .logDate(logDate)
                    .waterIntakeMl(logRequest.getWaterIntakeMl())
                    .glp1SideEffectLevel(logRequest.getGlp1SideEffectLevel())
                    .glp1SideEffects(logRequest.getGlp1SideEffects())
                    .lipedemaPainLevel(logRequest.getLipedemaPainLevel())
                    .glutenFreeCompliant(logRequest.getGlutenFreeCompliant())
                    .sugarFreeCompliant(logRequest.getSugarFreeCompliant())
                    .dairyFreeCompliant(logRequest.getDairyFreeCompliant())
                    .currentHormonalPhase(logRequest.getCurrentHormonalPhase())
                    .build();
        }

        DailyLog saved = dailyLogRepository.save(dailyLog);
        eventPublisher.publishEvent(new DailyLogSubmittedEvent(saved));
        return ResponseEntity.ok(saved);
    }

    // 2. Danışan: Kendi geçmiş günlük durumlarını listeler (Varsayılan son 14 gün)
    @GetMapping("/my")
    public ResponseEntity<?> getMyDailyLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(14);

        List<DailyLog> logs = dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(client.getId(), start, end);
        return ResponseEntity.ok(logs);
    }

    // 3. Diyetisyen: Danışanın günlük durumlarını çeker (Varsayılan son 14 gün)
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientDailyLogs(
            @PathVariable Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User client = clientOpt.get();
        // Yetki kontrolü (Diyetisyeni mi yoksa kendisi mi?)
        boolean isDietitian = dietitian.getRole() == Role.ROLE_DIETITIAN && client.getDietitian() != null && client.getDietitian().getId().equals(dietitian.getId());
        boolean isSelf = dietitian.getId().equals(clientId);

        if (!isDietitian && !isSelf) {
            return ResponseEntity.status(403).body("Bu verilere erişim yetkiniz bulunmuyor.");
        }

        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(14);

        List<DailyLog> logs = dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(clientId, start, end);
        return ResponseEntity.ok(logs);
    }
}
