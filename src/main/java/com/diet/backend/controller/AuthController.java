package com.diet.backend.controller;

import com.diet.backend.dto.JwtAuthResponse;
import com.diet.backend.dto.TokenRequest;
import com.diet.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<JwtAuthResponse> loginWithGoogle(@RequestBody TokenRequest tokenRequest) {
        String token = authService.loginWithGoogle(tokenRequest.getToken());
        return ResponseEntity.ok(JwtAuthResponse.builder()
                .accessToken(token)
                .build());
    }

    @PostMapping("/facebook")
    public ResponseEntity<JwtAuthResponse> loginWithFacebook(@RequestBody TokenRequest tokenRequest) {
        String token = authService.loginWithFacebook(tokenRequest.getToken());
        return ResponseEntity.ok(JwtAuthResponse.builder()
                .accessToken(token)
                .build());
    }
}
