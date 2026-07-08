package com.diet.backend.repository;

import com.diet.backend.model.Appointment;
import com.diet.backend.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientIdOrderByAppointmentDateDesc(Long clientId);
    List<Appointment> findByDietitianIdOrderByAppointmentDateDesc(Long dietitianId);
    List<Appointment> findByDietitianIdAndStatusOrderByAppointmentDateAsc(Long dietitianId, AppointmentStatus status);
}
