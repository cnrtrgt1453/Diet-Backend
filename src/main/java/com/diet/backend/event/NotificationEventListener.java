package com.diet.backend.event;

import com.diet.backend.model.Notification;
import com.diet.backend.model.User;
import com.diet.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleAppointmentStatusChanged(AppointmentStatusChangedEvent event) {
        User client = event.getAppointment().getClient();
        String statusText = event.getAppointment().getStatus().toString().equals("APPROVED") ? "onayladı" : "reddetti";
        String message = String.format("Diyetisyeniniz %s tarihindeki randevu talebinizi %s.",
                event.getAppointment().getAppointmentDate().toString(), statusText);

        Notification notification = Notification.builder()
                .recipient(client)
                .title("Randevu Durumu Güncellendi")
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("[MOCK PUSH NOTIFICATION] Sent to client (ID: {}): {} - {}", client.getId(), notification.getTitle(), notification.getMessage());
    }

    @EventListener
    public void handleDietPlanAssigned(DietPlanAssignedEvent event) {
        User client = event.getDietPlan().getClient();
        String message = String.format("Diyetisyeniniz sizin için yeni bir diyet planı oluşturdu: \"%s\" (%s)",
                event.getDietPlan().getTitle(), event.getDietPlan().getDate().toString());

        Notification notification = Notification.builder()
                .recipient(client)
                .title("Yeni Diyet Planı Atandı")
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("[MOCK PUSH NOTIFICATION] Sent to client (ID: {}): {} - {}", client.getId(), notification.getTitle(), notification.getMessage());
    }
}
