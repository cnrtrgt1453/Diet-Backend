package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diet_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private LocalDate date;

    private String title; // örn: "Pazartesi Detoksu"

    @Column(columnDefinition = "TEXT")
    private String breakfast;

    @Column(columnDefinition = "TEXT")
    private String lunch;

    @Column(columnDefinition = "TEXT")
    private String dinner;

    @Column(columnDefinition = "TEXT")
    private String snacks;

    private Integer targetCalories;

    @Builder.Default
    private Boolean completed = false;
}
