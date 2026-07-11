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
public class CategoryComplianceDto {
    private ClientCategory category;
    private Double complianceRate; // Yüzde olarak (0-100)
}
