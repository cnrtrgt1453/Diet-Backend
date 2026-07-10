package com.diet.backend.service;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.repository.MessageRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.NotificationRepository;
import com.diet.backend.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public List<Message> getChatHistory(User currentUser, Long otherUserId) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        User dietitian = currentUser.getRole() == Role.ROLE_DIETITIAN ? currentUser : currentUser.getDietitian();
        if (dietitian == null) {
            dietitian = otherUser;
        }

        List<Message> history = messageRepository.findChatHistory(currentUser, otherUser, dietitian);

        // Mark incoming private messages as read
        for (Message m : history) {
            if (!m.isBroadcast() && m.getRecipient().getId().equals(currentUser.getId()) && !m.isRead()) {
                m.setRead(true);
                messageRepository.save(m);
            }
        }

        return history;
    }

    @Transactional
    public Message sendPrivateMessage(User sender, Long recipientId, String content) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Alıcı bulunamadı"));

        // Validate dietitian/client association
        if (sender.getRole() == Role.ROLE_USER) {
            if (sender.getDietitian() == null || !sender.getDietitian().getId().equals(recipient.getId())) {
                throw new RuntimeException("Sadece kendi diyetisyeninize mesaj gönderebilirsiniz");
            }
        } else if (sender.getRole() == Role.ROLE_DIETITIAN) {
            if (recipient.getDietitian() == null || !recipient.getDietitian().getId().equals(sender.getId())) {
                throw new RuntimeException("Sadece kendinize bağlı danışanlara mesaj gönderebilirsiniz");
            }
        }

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .isBroadcast(false)
                .sentAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // Send notification
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title("Yeni Mesaj")
                .message(sender.getName() + ": " + content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        return savedMessage;
    }

    @Transactional
    public Message sendBroadcastMessage(User dietitian, String content) {
        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            throw new RuntimeException("Sadece diyetisyenler toplu mesaj gönderebilir");
        }

        Message message = Message.builder()
                .sender(dietitian)
                .recipient(null)
                .content(content)
                .isBroadcast(true)
                .sentAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // Send notification to all clients of this dietitian
        List<User> clients = userRepository.findByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER);
        for (User client : clients) {
            Notification notification = Notification.builder()
                    .recipient(client)
                    .title("Diyetisyeninizden Duyuru")
                    .message(content)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }

        return savedMessage;
    }
}
