package com.diet.backend.service;

/**
 * SRP: E-posta gönderim sorumluluğu.
 * AuthService'ten ayrıştırılarak tek sorumluluk prensibine uygun hale getirilmiştir.
 */
public interface MailService {

    /**
     * Şifre sıfırlama e-postası gönderir.
     *
     * @param toEmail Alıcı e-posta adresi
     * @param userName Kullanıcı adı
     */
    void sendPasswordResetEmail(String toEmail, String userName);
}
