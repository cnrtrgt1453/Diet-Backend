package com.diet.backend.service;

import java.util.Map;

/**
 * DIP: HTTP çağrılarını soyutlayan arayüz.
 * RestTemplate, WebClient veya OpenFeign gibi implementasyonlarla değiştirilebilir.
 */
public interface SocialAuthHttpClient {

    /**
     * Verilen URL'ye GET isteği yapar ve JSON yanıtını Map olarak döner.
     *
     * @param url Hedef URL
     * @return JSON yanıtı anahtar-değer çiftleri olarak
     * @throws RuntimeException HTTP isteği başarısız olursa
     */
    Map<String, Object> get(String url);
}
