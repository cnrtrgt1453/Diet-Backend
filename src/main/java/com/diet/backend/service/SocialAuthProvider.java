package com.diet.backend.service;

import com.diet.backend.dto.SocialUserInfo;
import com.diet.backend.model.Provider;

/**
 * OCP: Sosyal giriş sağlayıcı stratejisi arayüzü.
 * Her yeni sosyal giriş sağlayıcısı (Google, Facebook, Apple vb.)
 * bu arayüzü implemente eder; AuthService'e dokunulmaz.
 */
public interface SocialAuthProvider {

    /**
     * Bu provider'ın hangi sağlayıcıyı temsil ettiğini döner.
     */
    Provider getProvider();

    /**
     * Verilen token'ı doğrulayıp kullanıcı bilgilerini döner.
     *
     * @param token OAuth token (idToken veya accessToken)
     * @return Doğrulanmış kullanıcı bilgileri
     * @throws RuntimeException Token geçersizse veya API erişilemezse
     */
    SocialUserInfo authenticate(String token);
}
