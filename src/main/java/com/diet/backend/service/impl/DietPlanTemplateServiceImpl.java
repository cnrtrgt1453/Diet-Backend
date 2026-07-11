package com.diet.backend.service.impl;

import com.diet.backend.event.DietPlanAssignedEvent;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.DietPlan;
import com.diet.backend.model.DietPlanTemplate;
import com.diet.backend.model.User;
import com.diet.backend.repository.DietPlanRepository;
import com.diet.backend.repository.DietPlanTemplateRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.DietPlanTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DietPlanTemplateServiceImpl implements DietPlanTemplateService {

    private final DietPlanTemplateRepository templateRepository;
    private final DietPlanRepository dietPlanRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DietPlanTemplate saveTemplate(DietPlanTemplate templateRequest, User dietitian) {
        DietPlanTemplate template = DietPlanTemplate.builder()
                .dietitian(dietitian)
                .title(templateRequest.getTitle())
                .breakfast(templateRequest.getBreakfast())
                .lunch(templateRequest.getLunch())
                .dinner(templateRequest.getDinner())
                .snacks(templateRequest.getSnacks())
                .targetCalories(templateRequest.getTargetCalories())
                .targetProteinGrams(templateRequest.getTargetProteinGrams())
                .targetCarbsGrams(templateRequest.getTargetCarbsGrams())
                .targetFatGrams(templateRequest.getTargetFatGrams())
                .build();

        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public DietPlanTemplate createTemplateFromPlan(Long planId, String title, User dietitian) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyet planı bulunamadı."));

        // Diyetin diyetisyene ait olduğunu doğrula
        if (plan.getClient().getDietitian() == null || !plan.getClient().getDietitian().getId().equals(dietitian.getId())) {
            throw new IllegalArgumentException("Bu diyet planı size ait bir danışana tanımlanmamış.");
        }

        DietPlanTemplate template = DietPlanTemplate.builder()
                .dietitian(dietitian)
                .title(title != null ? title : plan.getTitle() + " Şablonu")
                .breakfast(plan.getBreakfast())
                .lunch(plan.getLunch())
                .dinner(plan.getDinner())
                .snacks(plan.getSnacks())
                .targetCalories(plan.getTargetCalories())
                .targetProteinGrams(plan.getTargetProteinGrams())
                .targetCarbsGrams(plan.getTargetCarbsGrams())
                .targetFatGrams(plan.getTargetFatGrams())
                .build();

        return templateRepository.save(template);
    }

    @Override
    public List<DietPlanTemplate> getTemplates(User dietitian) {
        return templateRepository.findByDietitianId(dietitian.getId());
    }

    @Override
    @Transactional
    public DietPlan assignTemplate(Long templateId, Long clientId, LocalDate date, User dietitian) {
        DietPlanTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyet şablonu bulunamadı."));

        // Şablon sahibi kontrolü
        if (!template.getDietitian().getId().equals(dietitian.getId())) {
            throw new IllegalArgumentException("Bu diyet şablonunu kullanmaya yetkiniz yok.");
        }

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışan bulunamadı."));

        // Danışanın diyetisyeni kontrolü
        if (client.getDietitian() == null || !client.getDietitian().getId().equals(dietitian.getId())) {
            throw new IllegalArgumentException("Bu danışan sizin takibinizde değil.");
        }

        DietPlan newDietPlan = DietPlan.builder()
                .client(client)
                .date(date != null ? date : LocalDate.now())
                .title(template.getTitle())
                .breakfast(template.getBreakfast())
                .lunch(template.getLunch())
                .dinner(template.getDinner())
                .snacks(template.getSnacks())
                .targetCalories(template.getTargetCalories())
                .targetProteinGrams(template.getTargetProteinGrams())
                .targetCarbsGrams(template.getTargetCarbsGrams())
                .targetFatGrams(template.getTargetFatGrams())
                .completed(false)
                .build();

        DietPlan saved = dietPlanRepository.save(newDietPlan);
        eventPublisher.publishEvent(new DietPlanAssignedEvent(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId, User dietitian) {
        DietPlanTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyet şablonu bulunamadı."));

        // Şablon sahibi kontrolü
        if (!template.getDietitian().getId().equals(dietitian.getId())) {
            throw new IllegalArgumentException("Bu diyet şablonunu silmeye yetkiniz yok.");
        }

        templateRepository.delete(template);
    }
}
