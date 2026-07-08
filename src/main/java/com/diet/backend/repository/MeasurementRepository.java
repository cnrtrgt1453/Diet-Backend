package com.diet.backend.repository;

import com.diet.backend.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByClientIdOrderByDateDesc(Long clientId);
    List<Measurement> findByClientIdOrderByDateAsc(Long clientId);
}
