package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dietitian_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietitianApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String university;
    private String diplomaNumber;
    private Integer experienceYears;
    private String documentUrl;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
