package com.diet.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password; // Sosyal giriş yapan kullanıcılar için boş kalabilir

    private String name;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Danışan Bilgileri (ROLE_USER için geçerli)
    private Double height;
    private Double currentWeight;
    private Double targetWeight;

    @Enumerated(EnumType.STRING)
    private ClientCategory category;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id")
    private User dietitian;

    // GLP-1 Takip Parametreleri
    private String glp1InjectionDay;
    private String glp1Dosage;

    // Lipödem Takip Parametreleri
    private Integer lipedemaStage;
    private Boolean antiInflammatoryCompliant;

    // Hormonal Takip Parametreleri
    private String hormoneTargetCycle;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus dietitianApplicationStatus;

    @Column(columnDefinition = "TEXT")
    private String dietitianRejectionReason;

    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private String profilePictureUrl;

    private String fcmToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
