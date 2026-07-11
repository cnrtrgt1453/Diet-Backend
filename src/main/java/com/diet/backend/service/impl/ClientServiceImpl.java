package com.diet.backend.service.impl;

import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.Measurement;
import com.diet.backend.model.DietPlan;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.MeasurementRepository;
import com.diet.backend.repository.DietPlanRepository;
import com.diet.backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final DietPlanRepository dietPlanRepository;

    @Override
    public List<User> getClients(User dietitian) {
        return userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
    }

    @Override
    @Transactional
    public User addClient(User clientRequest, User dietitian) {
        if (userRepository.existsByEmail(clientRequest.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresiyle kayıtlı bir kullanıcı zaten mevcut.");
        }

        User newClient = User.builder()
                .email(clientRequest.getEmail())
                .name(clientRequest.getName())
                .role(Role.ROLE_USER)
                .height(clientRequest.getHeight())
                .currentWeight(clientRequest.getCurrentWeight())
                .targetWeight(clientRequest.getTargetWeight())
                .category(clientRequest.getCategory())
                .notes(clientRequest.getNotes())
                .glp1InjectionDay(clientRequest.getGlp1InjectionDay())
                .glp1Dosage(clientRequest.getGlp1Dosage())
                .lipedemaStage(clientRequest.getLipedemaStage())
                .antiInflammatoryCompliant(clientRequest.getAntiInflammatoryCompliant())
                .hormoneTargetCycle(clientRequest.getHormoneTargetCycle())
                .dietitian(dietitian)
                .build();

        User savedClient = userRepository.save(newClient);

        // İlk ölçüm kaydını oluştur
        if (savedClient.getCurrentWeight() != null) {
            measurementRepository.save(Measurement.builder()
                    .client(savedClient)
                    .date(LocalDate.now())
                    .weight(savedClient.getCurrentWeight())
                    .note("İlk kayıt ölçümü.")
                    .build());
        }

        return savedClient;
    }

    @Override
    @Transactional
    public User updateClient(Long clientId, User clientRequest, User dietitian) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            throw new ResourceNotFoundException("Danışan bulunamadı veya yetkiniz yok.");
        }

        User existingClient = clientOpt.get();
        existingClient.setName(clientRequest.getName());
        existingClient.setHeight(clientRequest.getHeight());
        existingClient.setTargetWeight(clientRequest.getTargetWeight());
        existingClient.setCategory(clientRequest.getCategory());
        existingClient.setNotes(clientRequest.getNotes());
        existingClient.setGlp1InjectionDay(clientRequest.getGlp1InjectionDay());
        existingClient.setGlp1Dosage(clientRequest.getGlp1Dosage());
        existingClient.setLipedemaStage(clientRequest.getLipedemaStage());
        existingClient.setAntiInflammatoryCompliant(clientRequest.getAntiInflammatoryCompliant());
        existingClient.setHormoneTargetCycle(clientRequest.getHormoneTargetCycle());

        // Eğer ağırlık değiştiyse, ölçüm geçmişine yeni seans kaydı olarak ekleyebiliriz
        if (clientRequest.getCurrentWeight() != null && !clientRequest.getCurrentWeight().equals(existingClient.getCurrentWeight())) {
            existingClient.setCurrentWeight(clientRequest.getCurrentWeight());
            measurementRepository.save(Measurement.builder()
                    .client(existingClient)
                    .date(LocalDate.now())
                    .weight(clientRequest.getCurrentWeight())
                    .note("Güncelleme ölçümü.")
                    .build());
        }

        return userRepository.save(existingClient);
    }

    @Override
    @Transactional
    public void deleteClient(Long clientId, User dietitian) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            throw new ResourceNotFoundException("Danışan bulunamadı veya yetkiniz yok.");
        }

        // Önce bağlı ölçümleri ve diyetleri sil
        List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateDesc(clientId);
        measurementRepository.deleteAll(measurements);

        List<DietPlan> dietPlans = dietPlanRepository.findByClientIdOrderByDateDesc(clientId);
        dietPlanRepository.deleteAll(dietPlans);

        userRepository.delete(clientOpt.get());
    }
}
