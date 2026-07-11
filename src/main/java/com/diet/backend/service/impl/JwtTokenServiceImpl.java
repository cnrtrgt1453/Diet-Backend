package com.diet.backend.service.impl;

import com.diet.backend.security.JwtTokenProvider;
import com.diet.backend.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * SRP: JWT token üretimini JwtTokenProvider bileşenine delege eder.
 * Servis katmanından erişim bu sınıf aracılığıyla yapılır.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateTokenForUser(String email) {
        return jwtTokenProvider.generateTokenFromUsername(email);
    }
}
