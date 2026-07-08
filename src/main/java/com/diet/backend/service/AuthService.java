package com.diet.backend.service;

import com.diet.backend.model.Provider;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    public String loginWithGoogle(String idToken) {
        String email;
        String name;
        String providerId;

        // Mock test kolaylığı için
        if ("mock-token-google".equals(idToken)) {
            email = "test.google@dietapp.com";
            name = "Google Test Kullanıcısı";
            providerId = "google-123456789";
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
            email = "test.facebook@dietapp.com";
            name = "Facebook Test Kullanıcısı";
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
            user = User.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.ROLE_USER)
                    .build();
            user = userRepository.save(user);
        }

        return jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }
}
