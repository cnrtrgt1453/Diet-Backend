package com.diet.backend.service.impl;

import com.diet.backend.dto.*;
import com.diet.backend.exception.AccessDeniedException;
import com.diet.backend.exception.ResourceNotFoundException;
import com.diet.backend.model.*;
import com.diet.backend.repository.DailyLogRepository;
import com.diet.backend.repository.MeasurementRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsServiceImpl implements AdvancedAnalyticsService {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final DailyLogRepository dailyLogRepository;

    @Override
    public List<CohortDto> getCohortAnalysis(User dietitian) {
        List<User> clients = userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
        Map<String, List<User>> cohorts = new HashMap<>();

        for (User client : clients) {
            List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateAsc(client.getId());
            LocalDate startDate = measurements.isEmpty() ? LocalDate.now() : measurements.get(0).getDate();
            String cohortMonth = startDate.getYear() + "-" + String.format("%02d", startDate.getMonthValue());

            cohorts.computeIfAbsent(cohortMonth, k -> new ArrayList<>()).add(client);
        }

        List<CohortDto> cohortList = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : cohorts.entrySet()) {
            String month = entry.getKey();
            List<User> cohortClients = entry.getValue();

            double totalStartWeight = 0;
            double totalCurrentWeight = 0;
            int validClients = 0;

            for (User client : cohortClients) {
                List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateAsc(client.getId());
                if (!measurements.isEmpty()) {
                    double startWeight = measurements.get(0).getWeight();
                    double currentWeight = measurements.get(measurements.size() - 1).getWeight();
                    totalStartWeight += startWeight;
                    totalCurrentWeight += currentWeight;
                    validClients++;
                }
            }

            if (validClients > 0) {
                double avgStart = totalStartWeight / validClients;
                double avgCurrent = totalCurrentWeight / validClients;
                cohortList.add(CohortDto.builder()
                        .cohortMonth(month)
                        .totalClients((long) cohortClients.size())
                        .averageStartingWeight(Math.round(avgStart * 100.0) / 100.0)
                        .averageCurrentWeight(Math.round(avgCurrent * 100.0) / 100.0)
                        .averageWeightLoss(Math.round((avgStart - avgCurrent) * 100.0) / 100.0)
                        .build());
            } else {
                cohortList.add(CohortDto.builder()
                        .cohortMonth(month)
                        .totalClients((long) cohortClients.size())
                        .averageStartingWeight(0.0)
                        .averageCurrentWeight(0.0)
                        .averageWeightLoss(0.0)
                        .build());
            }
        }

        cohortList.sort(Comparator.comparing(CohortDto::getCohortMonth));
        return cohortList;
    }

    @Override
    public List<CategoryComplianceDto> getCategoryCompliance(User dietitian) {
        List<User> clients = userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
        Map<ClientCategory, List<Double>> categoryScores = new HashMap<>();

        for (User client : clients) {
            if (client.getCategory() == null) continue;
            List<DailyLog> logs = dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(
                    client.getId(), LocalDate.now().minusDays(30), LocalDate.now()
            );

            for (DailyLog log : logs) {
                double score = calculateDailyCompliance(log);
                categoryScores.computeIfAbsent(client.getCategory(), k -> new ArrayList<>()).add(score);
            }
        }

        List<CategoryComplianceDto> complianceList = new ArrayList<>();
        for (ClientCategory category : ClientCategory.values()) {
            List<Double> scores = categoryScores.get(category);
            double avgRate = 0.0;
            if (scores != null && !scores.isEmpty()) {
                avgRate = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            }
            complianceList.add(CategoryComplianceDto.builder()
                    .category(category)
                    .complianceRate(Math.round(avgRate * 100.0) / 100.0)
                    .build());
        }

        return complianceList;
    }

    @Override
    public List<ClientWeightLossRateDto> getClientWeightLossRates(User dietitian) {
        List<User> clients = userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
        List<ClientWeightLossRateDto> rates = new ArrayList<>();

        for (User client : clients) {
            List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateAsc(client.getId());
            if (measurements.size() >= 2) {
                Measurement start = measurements.get(0);
                Measurement end = measurements.get(measurements.size() - 1);

                long days = ChronoUnit.DAYS.between(start.getDate(), end.getDate());
                double weightLoss = start.getWeight() - end.getWeight();
                double ratePerWeek = 0.0;

                if (days > 0) {
                    ratePerWeek = weightLoss / (days / 7.0);
                }

                rates.add(ClientWeightLossRateDto.builder()
                        .clientName(client.getName())
                        .category(client.getCategory())
                        .startingWeight(start.getWeight())
                        .currentWeight(end.getWeight())
                        .weightLossRateKgPerWeek(Math.round(ratePerWeek * 100.0) / 100.0)
                        .daysTracked(days)
                        .build());
            } else {
                rates.add(ClientWeightLossRateDto.builder()
                        .clientName(client.getName())
                        .category(client.getCategory())
                        .startingWeight(client.getCurrentWeight() != null ? client.getCurrentWeight() : 0.0)
                        .currentWeight(client.getCurrentWeight() != null ? client.getCurrentWeight() : 0.0)
                        .weightLossRateKgPerWeek(0.0)
                        .daysTracked(0L)
                        .build());
            }
        }
        return rates;
    }

    @Override
    public CorrelationAnalysisDto getCorrelationAnalysis(Long clientId, User loggedInUser) {
        User client = verifyAndGetClient(clientId, loggedInUser);

        List<DailyLog> logs = dailyLogRepository.findByClientIdAndLogDateBetweenOrderByLogDateAsc(
                client.getId(), LocalDate.now().minusDays(30), LocalDate.now()
        );
        List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateAsc(client.getId());

        List<CorrelationPointDto> points = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Double> compliances = new ArrayList<>();
        List<Double> waterIntakes = new ArrayList<>();
        List<Double> activities = new ArrayList<>();

        for (DailyLog log : logs) {
            Double weight = findClosestWeight(log.getLogDate(), measurements);
            if (weight == null) continue;

            double compliance = calculateDailyCompliance(log);
            double water = log.getWaterIntakeMl() != null ? log.getWaterIntakeMl() : 0.0;
            double activity = log.getPhysicalActivityMinutes() != null ? log.getPhysicalActivityMinutes().doubleValue() : 0.0;

            points.add(CorrelationPointDto.builder()
                    .date(log.getLogDate())
                    .weight(weight)
                    .dietCompliancePercentage(compliance)
                    .waterIntakeMl(water)
                    .physicalActivityMinutes(log.getPhysicalActivityMinutes() != null ? log.getPhysicalActivityMinutes() : 0)
                    .build());

            weights.add(weight);
            compliances.add(compliance);
            waterIntakes.add(water);
            activities.add(activity);
        }

        double complianceCorr = calculatePearsonR(compliances, weights);
        double waterCorr = calculatePearsonR(waterIntakes, weights);
        double activityCorr = calculatePearsonR(activities, weights);

        return CorrelationAnalysisDto.builder()
                .dietComplianceCorrelation(Math.round(complianceCorr * 1000.0) / 1000.0)
                .waterIntakeCorrelation(Math.round(waterCorr * 1000.0) / 1000.0)
                .physicalActivityCorrelation(Math.round(activityCorr * 1000.0) / 1000.0)
                .dataPoints(points)
                .build();
    }

    @Override
    public WeightPredictionDto getWeightPrediction(Long clientId, User loggedInUser) {
        User client = verifyAndGetClient(clientId, loggedInUser);

        List<Measurement> measurements = measurementRepository.findByClientIdOrderByDateAsc(client.getId());
        if (measurements.size() < 2) {
            return WeightPredictionDto.builder()
                    .targetWeight(client.getTargetWeight())
                    .currentWeight(client.getCurrentWeight())
                    .statusMessage("Tahmin motoru için en az 2 ölçüm verisi bulunmalıdır.")
                    .build();
        }

        Measurement first = measurements.get(0);
        Measurement last = measurements.get(measurements.size() - 1);
        long totalDays = ChronoUnit.DAYS.between(first.getDate(), last.getDate());

        // Lineer Regresyon Hesaplaması (y = ax + b)
        // x: ilk ölçümden itibaren geçen gün sayısı
        // y: ölçülen kilo
        int n = measurements.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;

        for (Measurement m : measurements) {
            double x = ChronoUnit.DAYS.between(first.getDate(), m.getDate());
            double y = m.getWeight();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denominator = n * sumXX - sumX * sumX;
        if (denominator == 0) {
            return WeightPredictionDto.builder()
                    .targetWeight(client.getTargetWeight())
                    .currentWeight(last.getWeight())
                    .startingWeight(first.getWeight())
                    .daysTracked(totalDays)
                    .statusMessage("Tarihsel ölçümlerde yeterli zaman farkı bulunmuyor.")
                    .build();
        }

        double slope = (n * sumXY - sumX * sumY) / denominator; // Günlük kilo kaybı (a)
        double intercept = (sumY - slope * sumX) / denominator; // Başlangıç kilosu tahmini (b)

        // R-Squared (R2) Hesaplaması (Uyum kalitesi)
        double avgY = sumY / n;
        double ssTot = 0;
        double ssRes = 0;
        for (Measurement m : measurements) {
            double x = ChronoUnit.DAYS.between(first.getDate(), m.getDate());
            double y = m.getWeight();
            double yPred = slope * x + intercept;
            ssTot += Math.pow(y - avgY, 2);
            ssRes += Math.pow(y - yPred, 2);
        }
        double rSquared = ssTot == 0 ? 0.0 : 1.0 - (ssRes / ssTot);

        double target = client.getTargetWeight() != null ? client.getTargetWeight() : 60.0;
        double current = last.getWeight();
        double starting = first.getWeight();
        double weeklyRate = -slope * 7.0;

        if (slope >= 0) {
            return WeightPredictionDto.builder()
                    .targetWeight(target)
                    .currentWeight(current)
                    .startingWeight(starting)
                    .daysTracked(totalDays)
                    .averageWeightLossPerWeek(Math.round(weeklyRate * 100.0) / 100.0)
                    .regressionSlope(Math.round(slope * 10000.0) / 10000.0)
                    .regressionIntercept(Math.round(intercept * 100.0) / 100.0)
                    .rSquared(Math.round(rSquared * 100.0) / 100.0)
                    .statusMessage("Kilo kaybı trendi bulunamadı veya ağırlık artışı var. Hedef tarih hesaplanamıyor.")
                    .build();
        }

        // Gün hesabı: (HedefKilo - MevcutKilo) / slope
        double daysToTarget = (target - current) / slope;
        if (daysToTarget < 0) daysToTarget = 0; // Zaten hedefe ulaşıldıysa

        LocalDate predictedDate = LocalDate.now().plusDays((long) Math.ceil(daysToTarget));

        return WeightPredictionDto.builder()
                .targetWeight(target)
                .currentWeight(current)
                .startingWeight(starting)
                .daysTracked(totalDays)
                .averageWeightLossPerWeek(Math.round(weeklyRate * 100.0) / 100.0)
                .regressionSlope(Math.round(slope * 10000.0) / 10000.0)
                .regressionIntercept(Math.round(intercept * 100.0) / 100.0)
                .rSquared(Math.round(rSquared * 100.0) / 100.0)
                .predictedTargetDate(predictedDate)
                .estimatedDaysRemaining((long) Math.ceil(daysToTarget))
                .statusMessage("Başarılı analiz.")
                .build();
    }

    private User verifyAndGetClient(Long clientId, User loggedInUser) {
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
        return client;
    }

    private double calculateDailyCompliance(DailyLog log) {
        double compliantFlags = 0;
        double totalFlags = 5.0; // 5 compliant parametresi var

        if (Boolean.TRUE.equals(log.getGlutenFreeCompliant())) compliantFlags++;
        if (Boolean.TRUE.equals(log.getSugarFreeCompliant())) compliantFlags++;
        if (Boolean.TRUE.equals(log.getDairyFreeCompliant())) compliantFlags++;
        if (Boolean.TRUE.equals(log.getProcessedFoodFreeCompliant())) compliantFlags++;
        if (Boolean.TRUE.equals(log.getAlcoholFreeCompliant())) compliantFlags++;

        return (compliantFlags / totalFlags) * 100.0;
    }

    private Double findClosestWeight(LocalDate date, List<Measurement> measurements) {
        if (measurements.isEmpty()) return null;
        Measurement closest = measurements.get(0);
        long minDiff = Math.abs(ChronoUnit.DAYS.between(date, closest.getDate()));

        for (Measurement m : measurements) {
            long diff = Math.abs(ChronoUnit.DAYS.between(date, m.getDate()));
            if (diff < minDiff) {
                minDiff = diff;
                closest = m;
            }
        }
        return closest.getWeight();
    }

    private double calculatePearsonR(List<Double> x, List<Double> y) {
        int n = x.size();
        if (n < 2) return 0.0;

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        double sumYY = 0;

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumXX += xi * xi;
            sumYY += yi * yi;
        }

        double num = n * sumXY - sumX * sumY;
        double den = Math.sqrt((n * sumXX - sumX * sumX) * (n * sumYY - sumY * sumY));

        return den == 0 ? 0.0 : num / den;
    }
}
