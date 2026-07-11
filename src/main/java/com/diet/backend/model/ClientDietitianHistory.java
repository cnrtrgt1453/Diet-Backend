package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Danışan-diyetisyen ilişki değişiklik tarihçesi.
 * Soft-delete yerine tarihçe kaydı tutarak eski ilişkilerin kaybolmasını engeller.
 */
@Entity
@Table(name = "client_dietitian_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDietitianHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_dietitian_id")
    private User previousDietitian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_dietitian_id", nullable = false)
    private User newDietitian;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    /**
     * Değişiklik nedeni:
     * - CONNECTION_APPROVED: Danışan yeni diyetisyene bağlandı
     * - ADMIN_REASSIGN: Admin tarafından yeniden atama
     * - SOCIAL_LOGIN_DEFAULT: Sosyal giriş ile ilk kayıt, admin diyetisyene atama
     */
    @Column(nullable = false)
    private String reason;
}
