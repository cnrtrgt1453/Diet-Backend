package com.diet.backend.service.impl;

import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.Measurement;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.MeasurementRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeasurementServiceImpl implements MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;

    @Override
    public List<Measurement> getClientMeasurements(Long clientId, User loggedInUser) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new ResourceNotFoundException("Danışan bulunamadı.");
        }

        User client = clientOpt.get();
        boolean isDietitian = loggedInUser.getRole() == Role.ROLE_DIETITIAN && 
                client.getDietitian() != null && 
                client.getDietitian().getId().equals(loggedInUser.getId());
        boolean isSelf = loggedInUser.getId().equals(clientId);

        if (!isDietitian && !isSelf) {
            throw new AccessDeniedException("Bu verilere erişim yetkiniz bulunmuyor.");
        }

        return measurementRepository.findByClientIdOrderByDateDesc(clientId);
    }

    @Override
    @Transactional
    public Measurement addMeasurement(Long clientId, Measurement measurementRequest, User dietitian) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            throw new ResourceNotFoundException("Danışan bulunamadı veya yetkiniz yok.");
        }

        User client = clientOpt.get();

        Measurement newMeasurement = Measurement.builder()
                .client(client)
                .date(measurementRequest.getDate() != null ? measurementRequest.getDate() : LocalDate.now())
                .weight(measurementRequest.getWeight())
                .bodyFat(measurementRequest.getBodyFat())
                .muscleMass(measurementRequest.getMuscleMass())
                .ankleCircumference(measurementRequest.getAnkleCircumference())
                .calfCircumference(measurementRequest.getCalfCircumference())
                .thighCircumference(measurementRequest.getThighCircumference())
                .note(measurementRequest.getNote())
                .build();

        Measurement saved = measurementRepository.save(newMeasurement);

        // Danışanın son kilosunu güncelle
        client.setCurrentWeight(measurementRequest.getWeight());
        userRepository.save(client);

        return saved;
    }
}
