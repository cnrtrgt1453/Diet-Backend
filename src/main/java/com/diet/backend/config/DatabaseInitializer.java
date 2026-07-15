package com.diet.backend.config;

import com.diet.backend.model.*;
import com.diet.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DietitianApplicationRepository dietitianApplicationRepository;
    private final DietitianConnectionRequestRepository dietitianConnectionRequestRepository;
    private final AppointmentRepository appointmentRepository;
    private final DailyLogRepository dailyLogRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final ClientDietitianHistoryRepository clientDietitianHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.dietitian.email}")
    private String dietitianEmail;

    @Value("${app.db.init-clean:false}")
    private boolean initClean;

    @Override
    public void run(String... args) throws Exception {
        // Drop outdated check constraints on startup
        try {
            System.out.println("Dropping old dietitian application status check constraints if they exist...");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_dietitian_application_status_check");
            jdbcTemplate.execute("ALTER TABLE dietitian_applications DROP CONSTRAINT IF EXISTS dietitian_applications_status_check");
            System.out.println("Dropped old dietitian application status check constraints successfully.");
        } catch (Exception e) {
            System.err.println("Failed to drop check constraints: " + e.getMessage());
        }

        if (initClean || userRepository.findByEmail(dietitianEmail).isEmpty()) {
            System.out.println("Cleaning database...");
            notificationRepository.deleteAll();
            messageRepository.deleteAll();
            measurementRepository.deleteAll();
            appointmentRepository.deleteAll();
            dailyLogRepository.deleteAll();
            dietPlanRepository.deleteAll();
            clientDietitianHistoryRepository.deleteAll();
            dietitianConnectionRequestRepository.deleteAll();
            dietitianApplicationRepository.deleteAll();
            userRepository.deleteAll();

            System.out.println("Seeding admin dietitian: " + dietitianEmail);
            User adminDietitian = User.builder()
                    .email(dietitianEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .name("Admin Diyetisyen")
                    .role(Role.ROLE_DIETITIAN)
                    .notes("İzmir / Alsancak Kliniği")
                    .instagramUrl("https://instagram.com/suhedaterat")
                    .linkedinUrl("https://linkedin.com/in/suhedaterat")
                    .youtubeUrl("https://youtube.com/@suhedaterat")
                    .profilePictureUrl("https://images.unsplash.com/photo-1594824813573-246434de83fb?q=80&w=256&auto=format&fit=crop")
                    .build();
            userRepository.save(adminDietitian);

            // System.out.println("Seeding mock clients...");
            // seedMockClients(adminDietitian);
            System.out.println("Database initialization completed successfully (only admin dietitian seeded)!");
        }
    }

    private void seedMockClients(User dietitian) {
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

        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now().minusWeeks(2)).weight(81.2).bodyFat(34.2).muscleMass(26.5).ankleCircumference(22.5).calfCircumference(36.5).thighCircumference(56.0).note("İlk seans, motivasyonu yüksek.").build());
        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now().minusWeeks(1)).weight(79.8).bodyFat(33.5).muscleMass(26.4).ankleCircumference(22.2).calfCircumference(36.2).thighCircumference(55.4).note("İkinci hafta kontrolü. 0.25mg dozu 0.5mg'a çıkarıldı.").build());
        measurementRepository.save(Measurement.builder().client(client1).date(LocalDate.now()).weight(78.5).bodyFat(32.8).muscleMass(26.6).ankleCircumference(22.0).calfCircumference(36.0).thighCircumference(55.0).note("En son seans. Mide bulantısı geçti.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client1)
                .date(LocalDate.now())
                .title("GLP-1 Koruma Günü")
                .breakfast("2 adet haşlanmış yumurta, 1 dilim süzme peynir, bol yeşillik, 5 adet zeytin")
                .lunch("150g ızgara tavuk göğsü, fırınlanmış brokoli ve kabak, 1 kase yoğurt")
                .dinner("1 kase mercimek çorbası, bol yeşillikli zeytinyağlı salata")
                .snacks("10 adet çiğ badem, 1 fincan sade filtre kahve")
                .targetCalories(1300)
                .targetProteinGrams(110)
                .targetCarbsGrams(100)
                .targetFatGrams(45)
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

        measurementRepository.save(Measurement.builder().client(client2).date(LocalDate.now().minusWeeks(1)).weight(86.5).bodyFat(38.0).muscleMass(27.0).ankleCircumference(25.0).calfCircumference(41.0).thighCircumference(63.0).note("Lipödem bacak ağrısı şikayeti yoğun.").build());
        measurementRepository.save(Measurement.builder().client(client2).date(LocalDate.now()).weight(85.0).bodyFat(37.1).muscleMass(27.2).ankleCircumference(24.5).calfCircumference(40.2).thighCircumference(62.1).note("Glütensiz beslenme sonrası şişlikler azaldı.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client2)
                .date(LocalDate.now())
                .title("Anti-inflamatuar Lipödem Diyeti")
                .breakfast("Avokado ezmeli karabuğday patlağı, 2 adet haşlanmış yumurta, salatalık")
                .lunch("Zeytinyağlı taze fasulye, karabuğday pilavı, bol yeşillikli limonlu salata")
                .dinner("Izgara somon (fırında), zeytinyağlı fırın sebze (kuşkonmaz ve brüksel lahanası)")
                .snacks("1 porsiyon ananas (bromelain etkisi için ödem atıcı), yeşil çay")
                .targetCalories(1450)
                .targetProteinGrams(95)
                .targetCarbsGrams(120)
                .targetFatGrams(60)
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

        measurementRepository.save(Measurement.builder().client(client3).date(LocalDate.now().minusWeeks(1)).weight(100.0).bodyFat(28.5).muscleMass(38.2).ankleCircumference(23.5).calfCircumference(39.0).thighCircumference(60.0).note("İlk ölçüm, online takip başlangıcı.").build());
        measurementRepository.save(Measurement.builder().client(client3).date(LocalDate.now()).weight(98.2).bodyFat(27.6).muscleMass(38.5).ankleCircumference(23.0).calfCircumference(38.2).thighCircumference(59.1).note("İyi gidiyor, su tüketimi 3 litreye çıkarıldı.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client3)
                .date(LocalDate.now())
                .title("Kilo Verme Yağ Yakım Programı")
                .breakfast("3 yumurtalı mantarlı omlet, 2 dilim tam buğday ekmeği, beyaz peynir")
                .lunch("180g ızgara dana bonfile, fırında tatlı patates, mevsim salatası")
                .dinner("150g zeytinyağlı nohut yemeği, 4 yemek kaşığı bulgur pilavı, cacık")
                .snacks("1 adet muz, 1 avuç çiğ kabak çekirdeği")
                .targetCalories(1800)
                .targetProteinGrams(140)
                .targetCarbsGrams(160)
                .targetFatGrams(65)
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

        measurementRepository.save(Measurement.builder().client(client4).date(LocalDate.now()).weight(68.0).bodyFat(31.5).muscleMass(22.8).ankleCircumference(21.5).calfCircumference(34.0).thighCircumference(52.0).note("Başlangıç ölçümü yapıldı, tatlı krizleri yoğun.").build());

        dietPlanRepository.save(DietPlan.builder()
                .client(client4)
                .date(LocalDate.now())
                .title("Düşük Glisemik İndeks PCOS Diyeti")
                .breakfast("2 adet çırpılmış yumurta (zeytinyağında), 1/2 avokado, 1 dilim çavdar ekmeği")
                .lunch("150g fırında hindi göğsü, yeşil mercimek salatası (bol nar ekşili)")
                .dinner("Fırında kıymalı kabak yemeği, 1 kase ev yapımı kefir")
                .snacks("1 adet taze incir, 2 adet tam ceviz içi, melisa çayı")
                .targetCalories(1400)
                .targetProteinGrams(100)
                .targetCarbsGrams(120)
                .targetFatGrams(50)
                .completed(false)
                .build());

        System.out.println("Seeding mock daily logs...");
        seedDailyLogs(client1, client2, client3, client4);
    }

    private void seedDailyLogs(User c1, User c2, User c3, User c4) {
        for (int i = 14; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            
            // Client 1 (GLP-1) Daily Logs
            dailyLogRepository.save(DailyLog.builder()
                    .client(c1)
                    .logDate(date)
                    .waterIntakeMl(2000.0 + (i % 3) * 500)
                    .physicalActivityMinutes(30 + (i % 4) * 15)
                    .glutenFreeCompliant(i % 5 != 0)
                    .sugarFreeCompliant(i % 6 != 0)
                    .dairyFreeCompliant(true)
                    .processedFoodFreeCompliant(i % 4 != 0)
                    .alcoholFreeCompliant(true)
                    .glp1SideEffectLevel(1 + (i % 2))
                    .glp1NauseaSeverity(1)
                    .build());

            // Client 2 (Lipedema) Daily Logs
            dailyLogRepository.save(DailyLog.builder()
                    .client(c2)
                    .logDate(date)
                    .waterIntakeMl(2500.0 + (i % 2) * 500)
                    .physicalActivityMinutes(45 + (i % 3) * 15)
                    .glutenFreeCompliant(true)
                    .sugarFreeCompliant(i % 7 != 0)
                    .dairyFreeCompliant(true)
                    .processedFoodFreeCompliant(i % 5 != 0)
                    .alcoholFreeCompliant(true)
                    .lipedemaPainLevel(2 + (i % 2))
                    .lipedemaPainLevelVas(4 + (i % 3))
                    .build());

            // Client 3 (Weight Management) Daily Logs
            dailyLogRepository.save(DailyLog.builder()
                    .client(c3)
                    .logDate(date)
                    .waterIntakeMl(3000.0 + (i % 2) * 250)
                    .physicalActivityMinutes(60 + (i % 5) * 10)
                    .glutenFreeCompliant(true)
                    .sugarFreeCompliant(i % 4 != 0)
                    .dairyFreeCompliant(true)
                    .processedFoodFreeCompliant(i % 3 != 0)
                    .alcoholFreeCompliant(i % 8 != 0)
                    .build());

            // Client 4 (Hormonal Balance) Daily Logs
            dailyLogRepository.save(DailyLog.builder()
                    .client(c4)
                    .logDate(date)
                    .waterIntakeMl(1500.0 + (i % 4) * 250)
                    .physicalActivityMinutes(20 + (i % 3) * 10)
                    .glutenFreeCompliant(i % 3 != 0)
                    .sugarFreeCompliant(i % 3 != 0)
                    .dairyFreeCompliant(i % 4 != 0)
                    .processedFoodFreeCompliant(i % 5 != 0)
                    .alcoholFreeCompliant(true)
                    .fastingBloodGlucose(90.0 + (i % 3) * 5)
                    .insulinLevel(12.0 - (i % 3) * 2)
                    .cycleDay(1 + (14 - i))
                    .insulinCravingLevel(1 + (i % 3))
                    .build());
        }
    }
}
