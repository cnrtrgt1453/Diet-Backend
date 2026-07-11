package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightPredictionDto {
    private Double targetWeight;
    private Double currentWeight;
    private Double startingWeight;
    private Long daysTracked;
    private Double averageWeightLossPerWeek;
    private Double regressionSlope;
    private Double regressionIntercept;
    private Double rSquared;
    private LocalDate predictedTargetDate;
    private Long estimatedDaysRemaining;
    private String statusMessage;
}
