package com.diet.backend.service;

import com.diet.backend.model.*;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.DietPlanRepository;
import com.diet.backend.repository.MeasurementRepository;
import com.diet.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DietPlanRepository dietPlanRepository;
    private final MeasurementRepository measurementRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.dietitian.email}")
    private String dietitianEmail;

    private User getOrCreateDietitian() {
        return userRepository.findByEmail(dietitianEmail)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(dietitianEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .name("Diyetisyen Şüheda Terat")
                        .role(Role.ROLE_DIETITIAN)
                        .notes("İzmir / Alsancak Kliniği")
                        .instagramUrl("https://instagram.com/suhedaterat")
                        .linkedinUrl("https://linkedin.com/in/suhedaterat")
                        .youtubeUrl("https://youtube.com/@suhedaterat")
                        .profilePictureUrl("https://images.unsplash.com/photo-1594824813573-246434de83fb?q=80&w=256&auto=format&fit=crop")
                        .build()));
    }

    public String loginWithGoogle(String idToken) {
        String email;
        String name;
        String providerId;

        // Mock test kolaylığı için
        if ("mock-token-google".equals(idToken)) {
            email = "test.client.google@dietapp.com";
            name = "Danışan Google Test";
            providerId = "google-123456789";
        } else if ("mock-token-facebook".equals(idToken)) {
            email = "test.client.facebook@dietapp.com";
            name = "Danışan Facebook Test";
            providerId = "facebook-123456789";
        } else {
            try {
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response == null || response.containsKey("error_description")) {
                    throw new RuntimeException("Google token doğrulama başarısız");
                }
                email = (String) response.get("email");
                name = (String) response.get("name");
                providerId = (String) response.get("sub");
            } catch (Exception e) {
                throw new RuntimeException("Google API ile iletişim kurulamadı veya geçersiz jeton: " + e.getMessage());
            }
        }

        return getOrCreateUserAndGenerateToken(email, name, Provider.GOOGLE, providerId);
    }

    public String loginWithFacebook(String accessToken) {
        String email;
        String name;
        String providerId;

        // Mock test kolaylığı için
        if ("mock-token-facebook".equals(accessToken)) {
            email = "test.client.facebook@dietapp.com";
            name = "Danışan Facebook Test";
            providerId = "facebook-123456789";
        } else {
            try {
                String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response == null || response.containsKey("error")) {
                    throw new RuntimeException("Facebook token doğrulama başarısız");
                }
                email = (String) response.get("email");
                name = (String) response.get("name");
                providerId = (String) response.get("id");

                // Facebook bazen e-posta dönmeyebilir (izin verilmemişse). Bu durumda id ile geçici e-posta üretiyoruz.
                if (email == null) {
                    email = providerId + "@facebook.com";
                }
            } catch (Exception e) {
                throw new RuntimeException("Facebook API ile iletişim kurulamadı veya geçersiz jeton: " + e.getMessage());
            }
        }

        return getOrCreateUserAndGenerateToken(email, name, Provider.FACEBOOK, providerId);
    }

    private String getOrCreateUserAndGenerateToken(String email, String name, Provider provider, String providerId) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setName(name);
            user = userRepository.save(user);
        } else {
            User adminDietitian = userRepository.findByEmail(dietitianEmail).orElse(null);

            User.UserBuilder userBuilder = User.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.ROLE_USER)
                    .dietitian(adminDietitian);

            user = userBuilder.build();
            user = userRepository.save(user);
        }

        return jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    public String loginWithEmailAndPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-posta adresi veya şifre hatalı."));

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("E-posta adresi veya şifre hatalı.");
        }

        // Eğer rolü diyetisyen değilse ve bekleyen/reddedilen bir başvurusu da yoksa giriş yapamasın!
        if (user.getRole() != Role.ROLE_DIETITIAN && user.getDietitianApplicationStatus() == null) {
            throw new RuntimeException("Bu giriş alanı yalnızca diyetisyenler içindir.");
        }

        return jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu e-posta adresiyle kayıtlı bir kullanıcı bulunamadı."));

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(email);
            message.setSubject("DietApp - Şifre Sıfırlama Talebi");
            message.setText("Merhaba " + user.getName() + ",\n\n" +
                    "Hesabınızın şifresini sıfırlamak için aşağıdaki bağlantıyı kullanabilirsiniz:\n" +
                    "http://localhost:8080/api/v1/auth/reset-password?email=" + email + "\n\n" +
                    "Eğer bu talebi siz yapmadıysanız lütfen bu e-postayı dikkate almayınız.\n\n" +
                    "Sağlıklı günler dileriz,\nDietApp Ekibi");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("E-posta gönderimi başarısız oldu: " + e.getMessage());
            System.out.println("==================================================");
            System.out.println("MOCK MAIL SENDER - ŞİFRE SIFIRLAMA TALEBİ");
            System.out.println("Alıcı: " + email);
            System.out.println("Bağlantı: http://localhost:8080/api/v1/auth/reset-password?email=" + email);
            System.out.println("==================================================");
            throw new RuntimeException("E-posta gönderilemedi. Lütfen application.properties dosyasındaki SMTP / şifre ayarlarını kontrol ediniz. Hata: " + e.getMessage());
        }
    }
}
