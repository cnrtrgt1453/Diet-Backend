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
public class CorrelationPointDto {
    private LocalDate date;
    private Double weight;
    private Double dietCompliancePercentage;
    private Double waterIntakeMl;
    private Integer physicalActivityMinutes;
}
