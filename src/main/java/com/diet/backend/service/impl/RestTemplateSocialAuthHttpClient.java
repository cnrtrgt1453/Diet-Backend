package com.diet.backend.service.impl;

import com.diet.backend.service.SocialAuthHttpClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * RestTemplate tabanlı SocialAuthHttpClient implementasyonu.
 * İleride WebClient veya OpenFeign ile değiştirilebilir — DIP sayesinde
 * hiçbir tüketici sınıf etkilenmez.
 */
@Component
public class RestTemplateSocialAuthHttpClient implements SocialAuthHttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String url) {
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new RuntimeException("HTTP isteği boş yanıt döndü: " + url);
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("HTTP isteği başarısız: " + url + " — " + e.getMessage(), e);
        }
    }
}
