package com.diet.backend.service.impl;

import com.diet.backend.event.AppointmentStatusChangedEvent;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.*;
import com.diet.backend.repository.AppointmentRepository;
import com.diet.backend.repository.DietitianAvailabilityRepository;
import com.diet.backend.service.AppointmentAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentAvailabilityServiceImpl implements AppointmentAvailabilityService {

    private final DietitianAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DietitianAvailability createAvailabilitySlot(DietitianAvailability slotRequest, User dietitian) {
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            throw new IllegalArgumentException("Yalnızca diyetisyenler uygunluk slotu ekleyebilir.");
        }

        // Çakışan slot kontrolü
        List<DietitianAvailability> existingSlots = availabilityRepository.findByDietitianIdAndDate(
                dietitian.getId(), slotRequest.getDate()
        );
        for (DietitianAvailability existing : existingSlots) {
            if (existing.getStartTime().equals(slotRequest.getStartTime())) {
                throw new IllegalArgumentException("Bu saat slotu zaten tanımlanmış.");
            }
        }

        DietitianAvailability slot = DietitianAvailability.builder()
                .dietitian(dietitian)
                .date(slotRequest.getDate())
                .startTime(slotRequest.getStartTime())
                .endTime(slotRequest.getEndTime())
                .isBooked(false)
                .build();

        return availabilityRepository.save(slot);
    }

    @Override
    public List<DietitianAvailability> getAvailableSlots(Long dietitianId, LocalDate date) {
        return availabilityRepository.findByDietitianIdAndDateAndIsBooked(dietitianId, date, false);
    }

    @Override
    @Transactional
    public Appointment bookAppointmentBySlot(Long slotId, String note, User client) {
        if (client.getRole() != Role.ROLE_USER) {
            throw new IllegalArgumentException("Sadece danışanlar randevu alabilir.");
        }

        DietitianAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen randevu slotu bulunamadı."));

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new IllegalArgumentException("Bu randevu slotu zaten rezerve edilmiş.");
        }

        // Danışanın diyetisyeni kontrolü
        if (client.getDietitian() == null || !client.getDietitian().getId().equals(slot.getDietitian().getId())) {
            throw new IllegalArgumentException("Yalnızca kendi diyetisyeninizin tanımladığı slotlardan randevu alabilirsiniz.");
        }

        // Slotu rezerve et
        slot.setIsBooked(true);
        availabilityRepository.save(slot);

        // Randevuyu oluştur (Slot üzerinden alındığı için doğrudan ONAYLANDI (APPROVED) yapıyoruz)
        Appointment appointment = Appointment.builder()
                .client(client)
                .dietitian(slot.getDietitian())
                .appointmentDate(slot.getDate())
                .appointmentTime(slot.getStartTime())
                .note(note)
                .status(AppointmentStatus.APPROVED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        eventPublisher.publishEvent(new AppointmentStatusChangedEvent(saved));
        return saved;
    }

    @Override
    public List<DietitianAvailability> getDietitianUpcomingSlots(Long dietitianId) {
        return availabilityRepository.findByDietitianIdAndDateGreaterThanEqualAndIsBookedOrderByDateAscStartTimeAsc(
                dietitianId, LocalDate.now(), false
        );
    }

    @Override
    @Transactional
    public void deleteAvailabilitySlot(Long slotId, Long dietitianId) {
        DietitianAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot bulunamadı."));
        if (!slot.getDietitian().getId().equals(dietitianId)) {
            throw new IllegalArgumentException("Bu slotu silme yetkiniz bulunmamaktadır.");
        }
        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new IllegalArgumentException("Rezerve edilmiş slotlar silinemez.");
        }
        availabilityRepository.delete(slot);
    }
}
