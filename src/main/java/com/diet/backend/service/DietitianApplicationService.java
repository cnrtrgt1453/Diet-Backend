package com.diet.backend.service;

import com.diet.backend.dto.DietitianApplicationDto;
import com.diet.backend.model.*;
import com.diet.backend.repository.DietitianApplicationRepository;
import com.diet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietitianApplicationService {

    private final DietitianApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.dietitian.email}")
    private String adminEmail;

    @Transactional
    public DietitianApplication apply(DietitianApplicationDto dto) {
        // Zaten sistemde o e-postayla bir diyetisyen var mı kontrol et
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            if (user.getRole() == Role.ROLE_DIETITIAN) {
                throw new RuntimeException("Bu e-posta adresiyle zaten kayıtlı bir diyetisyen bulunmaktadır.");
            }
        });

        // Mevcut bekleyen başvurusu var mı kontrol et
        applicationRepository.findByEmail(dto.getEmail()).ifPresent(app -> {
            if (app.getStatus() == ApplicationStatus.PENDING) {
                throw new RuntimeException("Zaten bekleyen bir başvurunuz bulunmaktadır.");
            }
        });

        // Kullanıcıyı bul veya geçici olarak ROLE_USER (danışan) rolüyle oluştur
        User user = userRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(dto.getEmail())
                    .name(dto.getFullName())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(Role.ROLE_USER)
                    .dietitian(userRepository.findByEmail(adminEmail).orElse(null))
                    .build();
            user = userRepository.save(user);
        } else {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user = userRepository.save(user);
        }

        // Kullanıcının başvuru durumunu güncelle
        user.setDietitianApplicationStatus(ApplicationStatus.PENDING);
        user.setDietitianRejectionReason(null);
        userRepository.save(user);

        // Eski başvuru varsa sil veya güncelle, yoksa yeni oluştur
        DietitianApplication application = applicationRepository.findByEmail(dto.getEmail())
                .orElse(new DietitianApplication());

        application.setUser(user);
        application.setFullName(dto.getFullName());
        application.setEmail(dto.getEmail());
        application.setUniversity(dto.getUniversity());
        application.setDiplomaNumber(dto.getDiplomaNumber());
        application.setExperienceYears(dto.getExperienceYears());
        application.setDocumentUrl(dto.getDocumentUrl());
        application.setNote(dto.getNote());
        application.setStatus(ApplicationStatus.PENDING);
        application.setRejectionReason(null);
        application.setCreatedAt(LocalDateTime.now());
        application.setProcessedAt(null);

        return applicationRepository.save(application);
    }

    public List<DietitianApplication> getPendingApplications(User adminUser) {
        validateAdmin(adminUser);
        return applicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    @Transactional
    public DietitianApplication approveApplication(Long id, User adminUser) {
        validateAdmin(adminUser);

        DietitianApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı."));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Yalnızca beklemedeki başvurular onaylanabilir.");
        }

        // Kullanıcının rolünü diyetisyen olarak güncelle
        User user = application.getUser();
        user.setRole(Role.ROLE_DIETITIAN);
        user.setDietitian(null); // Kendi diyetisyen atamasını kaldır
        user.setDietitianApplicationStatus(ApplicationStatus.APPROVED);
        userRepository.save(user);

        // Başvuruyu güncelle
        application.setStatus(ApplicationStatus.APPROVED);
        application.setProcessedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    @Transactional
    public DietitianApplication rejectApplication(Long id, String reason, User adminUser) {
        validateAdmin(adminUser);

        DietitianApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı."));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Yalnızca beklemedeki başvurular reddedilebilir.");
        }

        // Kullanıcının durumunu güncelle
        User user = application.getUser();
        user.setDietitianApplicationStatus(ApplicationStatus.REJECTED);
        user.setDietitianRejectionReason(reason);
        userRepository.save(user);

        // Başvuruyu güncelle
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setProcessedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    private void validateAdmin(User user) {
        if (user == null || !adminEmail.equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bu işlemi yapmaya yetkiniz bulunmamaktadır. Yalnızca Admin Diyetisyen yetkilidir.");
        }
    }
}
