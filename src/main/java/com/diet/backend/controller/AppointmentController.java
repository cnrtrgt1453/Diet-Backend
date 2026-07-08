package com.diet.backend.controller;

import com.diet.backend.model.Appointment;
import com.diet.backend.model.AppointmentStatus;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.repository.AppointmentRepository;
import com.diet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    // 1. Danışan: Yeni randevu talebi oluşturur
    @PostMapping
    public ResponseEntity<?> requestAppointment(@RequestBody Appointment appointmentRequest) {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Sadece danışanlar randevu talep edebilir
        if (client.getRole() != Role.ROLE_USER) {
            return ResponseEntity.badRequest().body("Sadece danışanlar randevu talep edebilir.");
        }

        // Danışanın bir diyetisyeni olmalıdır
        if (client.getDietitian() == null) {
            return ResponseEntity.badRequest().body("Henüz atanmış bir diyetisyeniniz bulunmuyor.");
        }

        Appointment appointment = Appointment.builder()
                .client(client)
                .dietitian(client.getDietitian())
                .appointmentDate(appointmentRequest.getAppointmentDate() != null ? appointmentRequest.getAppointmentDate() : LocalDate.now())
                .appointmentTime(appointmentRequest.getAppointmentTime())
                .note(appointmentRequest.getNote())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    // 2. Danışan: Kendi randevu taleplerini listeler
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        User client = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appointment> appointments = appointmentRepository.findByClientIdOrderByAppointmentDateDesc(client.getId());
        return ResponseEntity.ok(appointments);
    }

    // 3. Diyetisyen: Kendisine gelen tüm randevuları veya durumuna göre filtrelenmiş randevuları listeler
    @GetMapping("/dietitian")
    public ResponseEntity<?> getDietitianAppointments(@RequestParam(required = false) AppointmentStatus status) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Bu işlem için yetkiniz bulunmamaktadır.");
        }

        List<Appointment> appointments;
        if (status != null) {
            appointments = appointmentRepository.findByDietitianIdAndStatusOrderByAppointmentDateAsc(dietitian.getId(), status);
        } else {
            appointments = appointmentRepository.findByDietitianIdOrderByAppointmentDateDesc(dietitian.getId());
        }
        return ResponseEntity.ok(appointments);
    }

    // 4. Diyetisyen: Randevuyu onaylar (APPROVED) veya reddeder (REJECTED)
    @PostMapping("/{appointmentId}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long appointmentId, @RequestParam AppointmentStatus status) {
        User dietitian = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            return ResponseEntity.status(403).body("Bu işlem için yetkiniz bulunmamaktadır.");
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = appointmentOpt.get();
        // Randevu bu diyetisyene mi atanmış kontrol et
        if (!appointment.getDietitian().getId().equals(dietitian.getId())) {
            return ResponseEntity.status(403).body("Bu randevuyu güncelleme yetkiniz bulunmuyor.");
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }
}
