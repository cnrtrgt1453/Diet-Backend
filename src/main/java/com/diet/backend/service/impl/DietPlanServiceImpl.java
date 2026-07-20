package com.diet.backend.service.impl;

import com.diet.backend.event.DietPlanAssignedEvent;
import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.DietPlan;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.DietPlanRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.DietPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DietPlanServiceImpl implements DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<DietPlan> getClientDiets(Long clientId, User loggedInUser) {
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
            throw new ResourceNotFoundException("Danışan bulunamadı veya yetkiniz yok.");
        }

        return dietPlanRepository.findByClientIdOrderByDateDesc(clientId);
    }

    @Override
    @Transactional
    public DietPlan addDietPlan(Long clientId, DietPlan dietPlanRequest, User dietitian) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty() || !clientOpt.get().getDietitian().getId().equals(dietitian.getId())) {
            throw new ResourceNotFoundException("Danışan bulunamadı veya yetkiniz yok.");
        }

        User client = clientOpt.get();

        DietPlan newDietPlan = DietPlan.builder()
                .client(client)
                .date(dietPlanRequest.getDate() != null ? dietPlanRequest.getDate() : LocalDate.now())
                .title(dietPlanRequest.getTitle())
                .breakfast(dietPlanRequest.getBreakfast())
                .lunch(dietPlanRequest.getLunch())
                .dinner(dietPlanRequest.getDinner())
                .snacks(dietPlanRequest.getSnacks())
                .targetCalories(dietPlanRequest.getTargetCalories())
                .targetProteinGrams(dietPlanRequest.getTargetProteinGrams())
                .targetCarbsGrams(dietPlanRequest.getTargetCarbsGrams())
                .targetFatGrams(dietPlanRequest.getTargetFatGrams())
                .completed(false)
                .build();

        DietPlan saved = dietPlanRepository.save(newDietPlan);
        eventPublisher.publishEvent(new DietPlanAssignedEvent(saved));
        return saved;
    }
}
