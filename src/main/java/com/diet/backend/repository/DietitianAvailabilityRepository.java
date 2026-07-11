package com.diet.backend.repository;

import com.diet.backend.model.DietitianAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DietitianAvailabilityRepository extends JpaRepository<DietitianAvailability, Long> {
    // Belirli bir tarihteki uygun (rezerve edilmemiş) slotları listeler
    List<DietitianAvailability> findByDietitianIdAndDateAndIsBooked(Long dietitianId, LocalDate date, Boolean isBooked);

    // Diyetisyenin ilgili tarihteki tüm slotlarını listeler
    List<DietitianAvailability> findByDietitianIdAndDate(Long dietitianId, LocalDate date);
}
