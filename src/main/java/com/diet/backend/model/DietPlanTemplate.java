package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diet_plan_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietPlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Şablonu oluşturan diyetisyen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private User dietitian;

    @Column(nullable = false)
    private String title; // Örn: "Düşük Karbonhidrat Şablonu"

    @Column(columnDefinition = "TEXT")
    private String breakfast;

    @Column(columnDefinition = "TEXT")
    private String lunch;

    @Column(columnDefinition = "TEXT")
    private String dinner;

    @Column(columnDefinition = "TEXT")
    private String snacks;

    private Integer targetCalories;
    private Integer targetProteinGrams;
    private Integer targetCarbsGrams;
    private Integer targetFatGrams;
}
