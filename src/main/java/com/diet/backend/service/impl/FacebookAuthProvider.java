package com.diet.backend.service.impl;

import com.diet.backend.dto.SocialUserInfo;
import com.diet.backend.model.Provider;
import com.diet.backend.service.SocialAuthHttpClient;
import com.diet.backend.service.SocialAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Facebook OAuth token doğrulama stratejisi.
 * Mock token desteği korunmaktadır (test kolaylığı).
 */
@Component
@RequiredArgsConstructor
public class FacebookAuthProvider implements SocialAuthProvider {

    private final SocialAuthHttpClient httpClient;

    @Override
    public Provider getProvider() {
        return Provider.FACEBOOK;
    }

    @Override
    public SocialUserInfo authenticate(String accessToken) {
        // Mock test kolaylığı
        if ("mock-token-facebook".equals(accessToken)) {
            return SocialUserInfo.builder()
                    .email("test.client.facebook@dietapp.com")
                    .name("Danışan Facebook Test")
                    .providerId("facebook-123456789")
                    .build();
        }

        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            Map<String, Object> response = httpClient.get(url);

            if (response.containsKey("error")) {
                throw new RuntimeException("Facebook token doğrulama başarısız");
            }

            String email = (String) response.get("email");
            String providerId = (String) response.get("id");

            // Facebook bazen e-posta dönmeyebilir (izin verilmemişse)
            if (email == null) {
                email = providerId + "@facebook.com";
            }

            return SocialUserInfo.builder()
                    .email(email)
                    .name((String) response.get("name"))
                    .providerId(providerId)
                    .build();
        } catch (RuntimeException e) {
            throw new RuntimeException("Facebook API ile iletişim kurulamadı veya geçersiz jeton: " + e.getMessage(), e);
        }
    }
}
