package com.diet.backend.service;

/**
 * SRP: JWT token üretimi ve doğrulama sorumluluğu.
 * AuthService'ten ayrıştırılarak tek sorumluluk prensibine uygun hale getirilmiştir.
 */
public interface JwtTokenService {

    /**
     * Verilen e-posta adresi için JWT token üretir.
     */
    String generateTokenForUser(String email);
}
