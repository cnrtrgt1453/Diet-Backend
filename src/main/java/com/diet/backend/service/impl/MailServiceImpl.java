package com.diet.backend.service.impl;

import com.diet.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * SRP: E-posta gönderim implementasyonu.
 * JavaMailSender bağımlılığı yalnızca bu sınıfta bulunur.
 * SMTP yapılandırması yoksa mock çıktı üretir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DietApp - Şifre Sıfırlama Talebi");
            message.setText("Merhaba " + userName + ",\n\n" +
                    "Hesabınızın şifresini sıfırlamak için aşağıdaki bağlantıyı kullanabilirsiniz:\n" +
                    "http://localhost:8080/api/v1/auth/reset-password?email=" + toEmail + "\n\n" +
                    "Eğer bu talebi siz yapmadıysanız lütfen bu e-postayı dikkate almayınız.\n\n" +
                    "Sağlıklı günler dileriz,\nDietApp Ekibi");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("E-posta gönderimi başarısız oldu: {}", e.getMessage());
            log.info("==================================================");
            log.info("MOCK MAIL SENDER - ŞİFRE SIFIRLAMA TALEBİ");
            log.info("Alıcı: {}", toEmail);
            log.info("Bağlantı: http://localhost:8080/api/v1/auth/reset-password?email={}", toEmail);
            log.info("==================================================");
            throw new RuntimeException(
                    "E-posta gönderilemedi. Lütfen application.properties dosyasındaki SMTP / şifre ayarlarını kontrol ediniz. Hata: " + e.getMessage());
        }
    }
}
