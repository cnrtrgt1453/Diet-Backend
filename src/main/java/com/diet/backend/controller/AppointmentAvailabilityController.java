package com.diet.backend.controller;

import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.Appointment;
import com.diet.backend.model.DietitianAvailability;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.service.AppointmentAvailabilityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentAvailabilityController {

    private final AppointmentAvailabilityService availabilityService;

    // Diyetisyen: Yeni uygunluk zaman slotu oluşturur
    @PostMapping("/availability")
    public ResponseEntity<?> createAvailabilitySlot(@RequestBody DietitianAvailability slotRequest) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Yalnızca diyetisyenler çalışma slotu ekleyebilir.");
        }

        DietitianAvailability saved = availabilityService.createAvailabilitySlot(slotRequest, dietitian);
        return ResponseEntity.ok(saved);
    }

    // Danışan/Diyetisyen: Belirli bir tarihteki boş slotları listeler
    @GetMapping("/availability/dietitian/{dietitianId}")
    public ResponseEntity<List<DietitianAvailability>> getAvailableSlots(
            @PathVariable Long dietitianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DietitianAvailability> slots = availabilityService.getAvailableSlots(dietitianId, date);
        return ResponseEntity.ok(slots);
    }

    // Danışan: Boş slot üzerinden randevu rezerve eder
    @PostMapping("/book-slot/{slotId}")
    public ResponseEntity<?> bookAppointmentBySlot(
            @PathVariable Long slotId,
            @RequestBody(required = false) BookingRequest request) {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (client.getRole() != Role.ROLE_USER) {
            return ResponseEntity.status(403).body("Sadece danışanlar randevu alabilir.");
        }

        String note = request != null ? request.getNote() : null;
        Appointment appointment = availabilityService.bookAppointmentBySlot(slotId, note, client);
        return ResponseEntity.ok(appointment);
    }

    // Diyetisyen: Giriş yapan diyetisyenin yaklaşan boş slotlarını listeler
    @GetMapping("/availability/my-slots")
    public ResponseEntity<List<DietitianAvailability>> getMySlots() {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body(null);
        }
        List<DietitianAvailability> slots = availabilityService.getDietitianUpcomingSlots(dietitian.getId());
        return ResponseEntity.ok(slots);
    }

    // Diyetisyen: Belirli bir boş slotu siler
    @DeleteMapping("/availability/{slotId}")
    public ResponseEntity<?> deleteAvailabilitySlot(@PathVariable Long slotId) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Yalnızca diyetisyenler çalışma slotu silebilir.");
        }
        availabilityService.deleteAvailabilitySlot(slotId, dietitian.getId());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class BookingRequest {
        private String note;
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
