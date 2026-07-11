package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dietitian_availabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietitianAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Slotu tanımlayan diyetisyen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private User dietitian;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String startTime; // Örn: "09:30"

    @Column(nullable = false)
    private String endTime; // Örn: "10:00"

    @Builder.Default
    @Column(nullable = false)
    private Boolean isBooked = false; // Slotun rezerve edilip edilmediği
}
