package com.diet.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/me")
    public ResponseEntity<String> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        return ResponseEntity.ok("Giriş Başarılı! E-posta: " + userDetails.getUsername() + ", Rol ve Yetkiler: " + userDetails.getAuthorities());
    }
}
