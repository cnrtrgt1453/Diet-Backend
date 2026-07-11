package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private LocalDate logDate;

    private Double waterIntakeMl;

    // GLP-1 Parametreleri
    private Integer glp1SideEffectLevel; // 1-5 arası
    private String glp1SideEffects;      // Örn: "Mide bulantısı, Halsizlik"
    private Integer glp1NauseaSeverity;
    private Integer glp1ConstipationSeverity;
    private Integer glp1DiarrheaSeverity;
    private Boolean glp1Vomiting;
    private String glp1InjectionSite;

    // Lipödem Parametreleri
    private Integer lipedemaPainLevel;   // 1-5 arası
    private Integer lipedemaPainLevelVas; // 1-10 VAS Skalası
    private Boolean glutenFreeCompliant;
    private Boolean sugarFreeCompliant;
    private Boolean dairyFreeCompliant;
    private Boolean processedFoodFreeCompliant;
    private Boolean alcoholFreeCompliant;

    // Hormon Parametreleri
    private String currentHormonalPhase; // Örn: "Foliküler Faz", "Luteal Faz"
    private Double fastingBloodGlucose;
    private Double insulinLevel;
    private Integer cycleDay;
    private Integer insulinCravingLevel;
}
