package com.diet.backend.controller;

import com.diet.backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        return ResponseEntity.ok(user);
    }
}
