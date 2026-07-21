package com.diet.backend.service.impl;

import com.diet.backend.dto.SocialUserInfo;
import com.diet.backend.model.Provider;
import com.diet.backend.service.SocialAuthHttpClient;
import com.diet.backend.service.SocialAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Google OAuth 2.0 token doğrulama stratejisi.
 * Mock token desteği korunmaktadır (test kolaylığı).
 */
@Component
@RequiredArgsConstructor
public class GoogleAuthProvider implements SocialAuthProvider {

    private final SocialAuthHttpClient httpClient;

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }

    @Override
    public SocialUserInfo authenticate(String idToken) {
        // Mock test kolaylığı
        if ("mock-token-google".equals(idToken)) {
            return SocialUserInfo.builder()
                    .email("test.client.google@dietapp.com")
                    .name("Danışan Google Test")
                    .providerId("google-123456789")
                    .pictureUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=256&auto=format&fit=crop")
                    .build();
        }

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            Map<String, Object> response = httpClient.get(url);

            if (response.containsKey("error_description")) {
                throw new RuntimeException("Google token doğrulama başarısız");
            }

            return SocialUserInfo.builder()
                    .email((String) response.get("email"))
                    .name((String) response.get("name"))
                    .providerId((String) response.get("sub"))
                    .pictureUrl((String) response.get("picture"))
                    .build();
        } catch (RuntimeException e) {
            throw new RuntimeException("Google API ile iletişim kurulamadı veya geçersiz jeton: " + e.getMessage(), e);
        }
    }
}
