package com.diet.backend.controller;

import com.diet.backend.model.Notification;
import com.diet.backend.model.User;
import com.diet.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // Kullanıcının kendi bildirimlerini getirir
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // Okunmamış bildirim sayısını getirir
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(count);
    }

    // Bildirimi okundu olarak işaretler
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Notification notification = notificationOpt.get();
        // Sadece bildirimin sahibi okundu olarak işaretleyebilir
        if (!notification.getRecipient().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Bu işlem için yetkiniz bulunmamaktadır.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    // Tüm bildirimleri okundu olarak işaretler
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
        return ResponseEntity.ok().build();
    }
}
