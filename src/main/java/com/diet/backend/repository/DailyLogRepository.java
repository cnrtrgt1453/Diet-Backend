package com.diet.backend.repository;

import com.diet.backend.model.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByClientIdAndLogDate(Long clientId, LocalDate logDate);
    List<DailyLog> findByClientIdAndLogDateBetweenOrderByLogDateAsc(Long clientId, LocalDate startDate, LocalDate endDate);
}
