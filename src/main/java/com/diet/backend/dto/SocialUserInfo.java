package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sosyal giriş sağlayıcılarından dönen standart kullanıcı bilgisi.
 * Strategy Pattern'deki her SocialAuthProvider bu DTO'yu döner.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialUserInfo {
    private String email;
    private String name;
    private String providerId;
}
