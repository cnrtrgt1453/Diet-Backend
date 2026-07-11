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
            Role userRole = Role.ROLE_USER;
            if (email.equals(dietitianEmail)) {
                userRole = Role.ROLE_DIETITIAN;
            }

            User.UserBuilder userBuilder = User.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .role(userRole);

            user = userBuilder.build();
            user = userRepository.save(user);

            // Diyetisyen hesabı ilk kez yaratıldığında hazır test verilerini (danışanlar, diyetler, ölçümler) yükle
            if (userRole == Role.ROLE_DIETITIAN) {
                createMockClientsForDietitian(user);
            }
        }

        return jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    private void createMockClientsForDietitian(User dietitian) {
        // 1. Danışan: GLP-1 Takip
        User client1 = User.builder()
                .email("ayse.yilmaz@gmail.com")
                .name("Ayşe Yılmaz")
                .role(Role.ROLE_USER)
                .height(165.0)
                .currentWeight(78.5)
                .targetWeight(65.0)
                .category(ClientCategory.GLP_1)
                .notes("GLP-1 tedavisine başlandı. Mide bulantısı hafif düzeyde, protein ağırlıklı beslenme uygulanıyor.")
                .glp1InjectionDay("Pazartesi")
                .glp1Dosage("0.5 mg")
                .dietitian(dietitian)
                .build();
        userRepository.save(client1);

        // Ayşe için geçmiş ölçümler
        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now().minusWeeks(2)).weight(81.2).bodyFat(34.2).muscleMass(26.5).note("İlk seans, motivasyonu yüksek.").build());
        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now().minusWeeks(1)).weight(79.8).bodyFat(33.5).muscleMass(26.4).note("İkinci hafta kontrolü. 0.25mg dozu 0.5mg'a çıkarıldı.").build());
        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now()).weight(78.5).bodyFat(32.8).muscleMass(26.6).note("En son seans. Mide bulantısı geçti.").build());

        // Ayşe için diyet planı
        dietPlanRepository.save(DietPlan.builder()
                .client(client1)
                .date(LocalDate.now())
                .title("GLP-1 Koruma Günü")
                .breakfast("2 adet haşlanmış yumurta, 1 dilim süzme peynir, bol yeşillik, 5 adet zeytin")
                .lunch("150g ızgara tavuk göğsü, fırınlanmış brokoli ve kabak, 1 kase yoğurt")
                .dinner("1 kase mercimek çorbası, bol yeşillikli zeytinyağlı salata")
                .snacks("10 adet çiğ badem, 1 fincan sade filtre kahve")
                .targetCalories(1300)
                .completed(true)
                .build());

        // 2. Danışan: Lipödem
        User client2 = User.builder()
                .email("zeynep.kaya@gmail.com")
                .name("Zeynep Kaya")
                .role(Role.ROLE_USER)
                .height(170.0)
                .currentWeight(85.0)
                .targetWeight(70.0)
                .category(ClientCategory.LIPEDEMA)
                .notes("Alsancak klinik yüz yüze danışan. Lipödem Evre 2. Glütensiz ve süt ürünsüz anti-inflamatuar beslenme programı.")
                .lipedemaStage(2)
                .antiInflammatoryCompliant(true)
                .dietitian(dietitian)
                .build();
        userRepository.save(client2);

        measurementRepository.save(Measurement.builder().client(client2).date(LocalDate.now().minusWeeks(1)).weight(86.5).bodyFat(38.0).muscleMass(27.0).note("Lipödem bacak ağrısı şikayeti yoğun.").build());
        measurementRepository.save(Measurement.builder().client(client2).date(LocalDate.now()).weight(85.0).bodyFat(37.1).muscleMass(27.2).note("Glütensiz beslenme sonrası şişlikler azaldı.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client2)
                .date(LocalDate.now())
                .title("Anti-inflamatuar Lipödem Diyeti")
                .breakfast("Avokado ezmeli karabuğday patlağı, 2 adet haşlanmış yumurta, salatalık")
                .lunch("Zeytinyağlı taze fasulye, karabuğday pilavı, bol yeşillikli limonlu salata")
                .dinner("Izgara somon (fırında), zeytinyağlı fırın sebze (kuşkonmaz ve brüksel lahanası)")
                .snacks("1 porsiyon ananas (bromelain etkisi için ödem atıcı), yeşil çay")
                .targetCalories(1450)
                .completed(false)
                .build());

        // 3. Danışan: Kilo Yönetimi
        User client3 = User.builder()
                .email("mehmet.demir@gmail.com")
                .name("Mehmet Demir")
                .role(Role.ROLE_USER)
                .height(180.0)
                .currentWeight(98.2)
                .targetWeight(80.0)
                .category(ClientCategory.WEIGHT_MANAGEMENT)
                .notes("Online danışan. Hedef kilo kaybı ve kardiyo egzersiz planı.")
                .dietitian(dietitian)
                .build();
        userRepository.save(client3);

        measurementRepository.save(Measurement.builder().client(client3).date(LocalDate.now().minusWeeks(1)).weight(100.0).bodyFat(28.5).muscleMass(38.2).note("İlk ölçüm, online takip başlangıcı.").build());
        measurementRepository.save(Measurement.builder().client(client3).date(LocalDate.now()).weight(98.2).bodyFat(27.6).muscleMass(38.5).note("İyi gidiyor, su tüketimi 3 litreye çıkarıldı.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client3)
                .date(LocalDate.now())
                .title("Kilo Verme Yağ Yakım Programı")
                .breakfast("3 yumurtalı mantarlı omlet, 2 dilim tam buğday ekmeği, beyaz peynir")
                .lunch("180g ızgara dana bonfile, fırında tatlı patates, mevsim salatası")
                .dinner("150g zeytinyağlı nohut yemeği, 4 yemek kaşığı bulgur pilavı, cacık")
                .snacks("1 adet muz, 1 avuç çiğ kabak çekirdeği")
                .targetCalories(1800)
                .completed(true)
                .build());

        // 4. Danışan: Hormonal Denge
        User client4 = User.builder()
                .email("elif.sahin@gmail.com")
                .name("Elif Şahin")
                .role(Role.ROLE_USER)
                .height(160.0)
                .currentWeight(68.0)
                .targetWeight(58.0)
                .category(ClientCategory.HORMONAL_BALANCE)
                .notes("PCOS takipli danışan. İnsülin direnci mevcut, glisemik indeksi düşük beslenme ve döngü takipli diyet programı.")
                .hormoneTargetCycle("Luteal Faz")
                .dietitian(dietitian)
                .build();
        userRepository.save(client4);

        measurementRepository.save(Measurement.builder().client(client4).date(LocalDate.now()).weight(68.0).bodyFat(31.5).muscleMass(22.8).note("Başlangıç ölçümü yapıldı, tatlı krizleri yoğun.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client4)
                .date(LocalDate.now())
                .title("Düşük Glisemik İndeks PCOS Diyeti")
                .breakfast("2 adet çırpılmış yumurta (zeytinyağında), 1/2 avokado, 1 dilim çavdar ekmeği")
                .lunch("150g fırında hindi göğsü, yeşil mercimek salatası (bol nar ekşili)")
                .dinner("Fırında kıymalı kabak yemeği, 1 kase ev yapımı kefir")
                .snacks("1 adet taze incir, 2 adet tam ceviz içi, melisa çayı")
                .targetCalories(1400)
                .completed(false)
                .build());
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
