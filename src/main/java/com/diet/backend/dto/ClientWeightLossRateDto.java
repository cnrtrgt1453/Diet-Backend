package com.diet.backend.dto;

import com.diet.backend.model.ClientCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientWeightLossRateDto {
    private String clientName;
    private ClientCategory category;
    private Double startingWeight;
    private Double currentWeight;
    private Double weightLossRateKgPerWeek;
    private Long daysTracked;
}
