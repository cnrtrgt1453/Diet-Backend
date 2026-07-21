package com.diet.backend.repository;

import com.diet.backend.model.DietitianAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DietitianAvailabilityRepository extends JpaRepository<DietitianAvailability, Long> {
    // Belirli bir tarihteki uygun (rezerve edilmemiş) slotları listeler
    List<DietitianAvailability> findByDietitianIdAndDateAndIsBooked(Long dietitianId, LocalDate date, Boolean isBooked);

    // Diyetisyenin ilgili tarihteki tüm slotlarını listeler
    List<DietitianAvailability> findByDietitianIdAndDate(Long dietitianId, LocalDate date);

    // Diyetisyenin bugünden itibaren tüm boş slotlarını listeler
    List<DietitianAvailability> findByDietitianIdAndDateGreaterThanEqualAndIsBookedOrderByDateAscStartTimeAsc(Long dietitianId, LocalDate date, Boolean isBooked);

    // Belirli bir tarih aralığındaki uygun (rezerve edilmemiş) slotları listeler
    List<DietitianAvailability> findByDietitianIdAndDateBetweenAndIsBookedOrderByDateAscStartTimeAsc(Long dietitianId, LocalDate startDate, LocalDate endDate, Boolean isBooked);

    // Diyetisyene, tarihe ve saate göre slot bulur
    Optional<DietitianAvailability> findByDietitianIdAndDateAndStartTime(Long dietitianId, LocalDate date, String startTime);
}
