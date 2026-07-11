package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortDto {
    private String cohortMonth; // Örn: "2026-07"
    private Long totalClients;
    private Double averageStartingWeight;
    private Double averageCurrentWeight;
    private Double averageWeightLoss;
}
