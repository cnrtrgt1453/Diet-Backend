package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationAnalysisDto {
    private Double dietComplianceCorrelation; // Pearson r
    private Double waterIntakeCorrelation;     // Pearson r
    private Double physicalActivityCorrelation; // Pearson r
    private List<CorrelationPointDto> dataPoints;
}
