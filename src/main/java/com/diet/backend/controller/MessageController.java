package com.diet.backend.controller;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import com.diet.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/inbox")
    public ResponseEntity<List<com.diet.backend.dto.ConversationSummary>> getInbox(
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(messageService.getInbox(currentUser));
    }

    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<Message>> getChatHistory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long otherUserId
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(messageService.getChatHistory(currentUser, otherUserId));
    }

    @PostMapping("/send/{recipientId}")
    public ResponseEntity<?> sendPrivateMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long recipientId,
            @RequestBody com.diet.backend.dto.MessageRequest request
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            Message message = messageService.sendPrivateMessage(currentUser, recipientId, request.getContent());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> sendBroadcastMessage(
            @AuthenticationPrincipal User currentUser,
            @RequestBody com.diet.backend.dto.MessageRequest request
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmalısınız!");
        }
        try {
            Message message = messageService.sendBroadcastMessage(currentUser, request.getContent());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
