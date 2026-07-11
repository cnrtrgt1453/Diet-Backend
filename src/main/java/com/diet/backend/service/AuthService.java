package com.diet.backend.service;

import com.diet.backend.model.Provider;

/**
 * ISP + SRP: Kimlik doğrulama sorumluluğu arayüzü.
 * Sosyal giriş, e-posta/şifre girişi ve şifre sıfırlama işlemlerini tanımlar.
 * Mail gönderimi MailService'e, JWT üretimi JwtTokenService'e,
 * sosyal giriş doğrulaması SocialAuthProvider stratejilerine delege edilmiştir.
 */
public interface AuthService {

    /**
     * Sosyal giriş sağlayıcısı ile oturum açar.
     * OCP: Yeni provider eklerken bu metot değişmez, yeni SocialAuthProvider eklenir.
     *
     * @param provider Sağlayıcı tipi (GOOGLE, FACEBOOK vb.)
     * @param token    OAuth token
     * @return JWT erişim token'ı
     */
    String loginWithSocial(Provider provider, String token);

    /**
     * E-posta ve şifre ile oturum açar (diyetisyen girişi).
     */
    String loginWithEmailAndPassword(String email, String password);

    /**
     * Şifre sıfırlama e-postası gönderir.
     */
    void forgotPassword(String email);
}
