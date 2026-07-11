package com.diet.backend.service.impl;

import com.diet.backend.event.DailyLogSubmittedEvent;
import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.DailyLog;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.DailyLogRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyLogServiceImpl implements DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DailyLog saveDailyLog(DailyLog logRequest, User client) {
        if (client.getRole() != Role.ROLE_USER) {
            throw new IllegalArgumentException("Sadece danışanlar günlük durum kaydı girebilir.");
        }

        LocalDate logDate = logRequest.getLogDate() != null ? logRequest.getLogDate() : LocalDate.now();
        Optional<DailyLog> existingLogOpt = dailyLogRepository.findByClientIdAndLogDate(client.getId(), logDate);

        DailyLog dailyLog;
        if (existingLogOpt.isPresent()) {
            dailyLog = existingLogOpt.get();
            dailyLog.setWaterIntakeMl(logRequest.getWaterIntakeMl());
            dailyLog.setGlp1SideEffectLevel(logRequest.getGlp1SideEffectLevel());
            dailyLog.setGlp1SideEffects(logRequest.getGlp1SideEffects());
            dailyLog.setGlp1NauseaSeverity(logRequest.getGlp1NauseaSeverity());
            dailyLog.setGlp1ConstipationSeverity(logRequest.getGlp1ConstipationSeverity());
            dailyLog.setGlp1DiarrheaSeverity(logRequest.getGlp1DiarrheaSeverity());
            dailyLog.setGlp1Vomiting(logRequest.getGlp1Vomiting());
            dailyLog.setGlp1InjectionSite(logRequest.getGlp1InjectionSite());
            dailyLog.setLipedemaPainLevel(logRequest.getLipedemaPainLevel());
            dailyLog.setLipedemaPainLevelVas(logRequest.getLipedemaPainLevelVas());
            dailyLog.setGlutenFreeCompliant(logRequest.getGlutenFreeCompliant());
            dailyLog.setSugarFreeCompliant(logRequest.getSugarFreeCompliant());
            dailyLog.setDairyFreeCompliant(logRequest.getDairyFreeCompliant());
            dailyLog.setProcessedFoodFreeCompliant(logRequest.getProcessedFoodFreeCompliant());
            dailyLog.setAlcoholFreeCompliant(logRequest.getAlcoholFreeCompliant());
            dailyLog.setCurrentHormonalPhase(logRequest.getCurrentHormonalPhase());
            dailyLog.setFastingBloodGlucose(logRequest.getFastingBloodGlucose());
            dailyLog.setInsulinLevel(logRequest.getInsulinLevel());
            dailyLog.setCycleDay(logRequest.getCycleDay());
            dailyLog.setInsulinCravingLevel(logRequest.getInsulinCravingLevel());
            // Yeni fiziksel aktivite alanı
            dailyLog.setPhysicalActivityMinutes(logRequest.getPhysicalActivityMinutes());
        } else {
            dailyLog = DailyLog.builder()
                    .client(client)
                    .logDate(logDate)
                    .waterIntakeMl(logRequest.getWaterIntakeMl())
                    .glp1SideEffectLevel(logRequest.getGlp1SideEffectLevel())
                    .glp1SideEffects(logRequest.getGlp1SideEffects())
                    .glp1NauseaSeverity(logRequest.getGlp1NauseaSeverity())
                    .glp1ConstipationSeverity(logRequest.getGlp1ConstipationSeverity())
                    .glp1DiarrheaSeverity(logRequest.getGlp1DiarrheaSeverity())
                    .glp1Vomiting(logRequest.getGlp1Vomiting())
                    .glp1InjectionSite(logRequest.getGlp1InjectionSite())
                    .lipedemaPainLevel(logRequest.getLipedemaPainLevel())
                    .lipedemaPainLevelVas(logRequest.getLipedemaPainLevelVas())
                    .glutenFreeCompliant(logRequest.getGlutenFreeCompliant())
                    .sugarFreeCompliant(logRequest.getSugarFreeCompliant())
                    .dairyFreeCompliant(logRequest.getDairyFreeCompliant())
                    .processedFoodFreeCompliant(logRequest.getProcessedFoodFreeCompliant())
                    .alcoholFreeCompliant(logRequest.getAlcoholFreeCompliant())
                    .currentHormonalPhase(logRequest.getCurrentHormonalPhase())
                    .fastingBloodGlucose(logRequest.getFastingBloodGlucose())
                    .insulinLevel(logRequest.getInsulinLevel())
                    .cycleDay(logRequest.getCycleDay())
                    .insulinCravingLevel(logRequest.getInsulinCravingLevel())
                    // Yeni fiziksel aktivite alanı
                    .physicalActivityMinutes(logRequest.getPhysicalActivityMinutes())
                    .build();
        }

        DailyLog saved = dailyLogRepository.save(dailyLog);
        eventPublisher.publishEvent(new DailyLogSubmittedEvent(saved));
        return saved;
    }

    @Override
    public List<DailyLog> getMyDailyLogs(LocalDate startDate, LocalDate endDate, User client) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(14);

        return dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(client.getId(), start, end);
    }

    @Override
    public List<DailyLog> getClientDailyLogs(Long clientId, LocalDate startDate, LocalDate endDate, User dietitian) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new ResourceNotFoundException("Danışan bulunamadı.");
        }

        User client = clientOpt.get();
        boolean isDietitian = dietitian.getRole() == Role.ROLE_DIETITIAN && 
                client.getDietitian() != null && 
                client.getDietitian().getId().equals(dietitian.getId());
        boolean isSelf = dietitian.getId().equals(clientId);

        if (!isDietitian && !isSelf) {
            throw new AccessDeniedException("Bu verilere erişim yetkiniz bulunmuyor.");
        }

        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(14);

        return dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(clientId, start, end);
    }
}
