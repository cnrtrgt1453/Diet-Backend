package com.diet.backend.service.impl;

import com.diet.backend.dto.SocialUserInfo;
import com.diet.backend.model.*;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.diet.backend.repository.ClientDietitianHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SRP + OCP + DIP uyumlu AuthService implementasyonu.
 *
 * - Sosyal giriş doğrulaması: SocialAuthProvider stratejilerine delege edilir (OCP).
 * - JWT üretimi: JwtTokenService'e delege edilir (SRP).
 * - E-posta gönderimi: MailService'e delege edilir (SRP).
 * - HTTP çağrıları: SocialAuthHttpClient soyutlaması arkasında (DIP).
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final List<SocialAuthProvider> socialAuthProviders;
    private final ClientDietitianHistoryRepository historyRepository;

    @Value("${app.dietitian.email}")
    private String adminDietitianEmail;

    @Override
    @Transactional
    public String loginWithSocial(Provider provider, String token) {
        // OCP: Provider'a uygun stratejiyi bul — yeni provider eklendiğinde bu kod değişmez
        SocialAuthProvider authProvider = socialAuthProviders.stream()
                .filter(p -> p.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Desteklenmeyen sosyal giriş sağlayıcısı: " + provider));

        SocialUserInfo userInfo = authProvider.authenticate(token);
        return getOrCreateUserAndGenerateToken(userInfo, provider);
    }

    @Override
    public String loginWithEmailAndPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-posta adresi veya şifre hatalı."));

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("E-posta adresi veya şifre hatalı.");
        }

        // Eğer rolü diyetisyen değilse ve bekleyen/reddedilen bir başvurusu da yoksa giriş yapamasın
        if (user.getRole() != Role.ROLE_DIETITIAN && user.getDietitianApplicationStatus() == null) {
            throw new RuntimeException("Bu giriş alanı yalnızca diyetisyenler içindir.");
        }

        return jwtTokenService.generateTokenForUser(user.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu e-posta adresiyle kayıtlı bir kullanıcı bulunamadı."));

        mailService.sendPasswordResetEmail(email, user.getName());
    }

    /**
     * Sosyal giriş sonrası kullanıcıyı bulur veya oluşturur ve JWT token üretir.
     * Yeni kullanıcılar ROLE_USER olarak oluşturulur ve admin diyetisyene atanır.
     */
    private String getOrCreateUserAndGenerateToken(SocialUserInfo userInfo, Provider provider) {
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setProvider(provider);
            user.setProviderId(userInfo.getProviderId());
            user.setName(userInfo.getName());
            user = userRepository.save(user);
        } else {
            User adminDietitian = userRepository.findByEmail(adminDietitianEmail).orElse(null);

            user = User.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .provider(provider)
                    .providerId(userInfo.getProviderId())
                    .role(Role.ROLE_USER)
                    .dietitian(adminDietitian)
                    .build();
            user = userRepository.save(user);

            // Log the initial dietitian assignment to history
            if (adminDietitian != null) {
                ClientDietitianHistory history = ClientDietitianHistory.builder()
                        .client(user)
                        .previousDietitian(null)
                        .newDietitian(adminDietitian)
                        .changedAt(LocalDateTime.now())
                        .reason("SOCIAL_LOGIN_DEFAULT")
                        .build();
                historyRepository.save(history);
            }
        }

        return jwtTokenService.generateTokenForUser(user.getEmail());
    }
}
