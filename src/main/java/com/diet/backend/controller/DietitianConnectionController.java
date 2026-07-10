package com.diet.backend.controller;

import com.diet.backend.model.DietitianConnectionRequest;
import com.diet.backend.model.User;
import com.diet.backend.service.DietitianConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
public class DietitianConnectionController {

    private final DietitianConnectionService connectionService;

    @GetMapping("/dietitians")
    public ResponseEntity<List<User>> getAllDietitians() {
        return ResponseEntity.ok(connectionService.getAllDietitians());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<DietitianConnectionRequest>> getMyRequests(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(connectionService.getClientRequests(currentUser));
    }

    @PostMapping("/request/{dietitianId}")
    public ResponseEntity<?> sendRequest(@AuthenticationPrincipal User currentUser, @PathVariable Long dietitianId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            DietitianConnectionRequest request = connectionService.sendConnectionRequest(currentUser, dietitianId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/pending-requests")
    public ResponseEntity<List<DietitianConnectionRequest>> getPendingRequests(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(connectionService.getPendingRequestsForDietitian(currentUser));
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@AuthenticationPrincipal User currentUser, @PathVariable Long requestId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            connectionService.approveRequest(currentUser, requestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@AuthenticationPrincipal User currentUser, @PathVariable Long requestId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            connectionService.rejectRequest(currentUser, requestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
