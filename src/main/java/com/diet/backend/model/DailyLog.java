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

    // Lipödem Parametreleri
    private Integer lipedemaPainLevel;   // 1-5 arası
    private Boolean glutenFreeCompliant;
    private Boolean sugarFreeCompliant;
    private Boolean dairyFreeCompliant;

    // Hormon Parametreleri
    private String currentHormonalPhase; // Örn: "Foliküler Faz", "Luteal Faz"
}
