package com.diet.backend.service;

import com.diet.backend.model.Appointment;
import com.diet.backend.model.DietitianAvailability;
import com.diet.backend.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentAvailabilityService {
    // Diyetisyen: Yeni uygunluk zaman slotu oluşturur
    DietitianAvailability createAvailabilitySlot(DietitianAvailability slotRequest, User dietitian);

    // Danışan/Diyetisyen: Belirli bir tarihteki rezerve edilmemiş boş slotları listeler
    List<DietitianAvailability> getAvailableSlots(Long dietitianId, LocalDate date);

    // Danışan/Diyetisyen: Belirli bir tarih aralığındaki rezerve edilmemiş boş slotları listeler
    List<DietitianAvailability> getAvailableSlotsInRange(Long dietitianId, LocalDate startDate, LocalDate endDate);

    // Danışan: Boş slot üzerinden randevu rezerve eder
    Appointment bookAppointmentBySlot(Long slotId, String note, User client);

    List<DietitianAvailability> getDietitianUpcomingSlots(Long dietitianId);

    void deleteAvailabilitySlot(Long slotId, Long dietitianId);
}
