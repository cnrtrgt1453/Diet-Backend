package com.diet.backend.controller;

import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.DailyLog;
import com.diet.backend.model.User;
import com.diet.backend.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs/daily")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // 1. Danışan: Günlük durumunu kaydeder (Varsa günceller, yoksa ekler)
    @PostMapping
    public ResponseEntity<?> saveDailyLog(@RequestBody DailyLog logRequest) {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DailyLog saved = dailyLogService.saveDailyLog(logRequest, client);
        return ResponseEntity.ok(saved);
    }

    // 2. Danışan: Kendi geçmiş günlük durumlarını listeler (Varsayılan son 14 gün)
    @GetMapping("/my")
    public ResponseEntity<?> getMyDailyLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DailyLog> logs = dailyLogService.getMyDailyLogs(startDate, endDate, client);
        return ResponseEntity.ok(logs);
    }

    // 3. Diyetisyen: Danışanın günlük durumlarını çeker (Varsayılan son 14 gün)
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientDailyLogs(
            @PathVariable Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DailyLog> logs = dailyLogService.getClientDailyLogs(clientId, startDate, endDate, dietitian);
        return ResponseEntity.ok(logs);
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
