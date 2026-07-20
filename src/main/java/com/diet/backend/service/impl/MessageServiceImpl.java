package com.diet.backend.service.impl;

import com.diet.backend.dto.ConversationSummary;
import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.model.Notification;
import com.diet.backend.repository.MessageRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.NotificationRepository;
import com.diet.backend.service.MessageService;
import com.diet.backend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    @Override
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

    @Override
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

        // FCM Push Notification
        if (recipient.getFcmToken() != null && !recipient.getFcmToken().isEmpty()) {
            try {
                fcmService.sendPushNotification(recipient.getFcmToken(), "Yeni Mesaj", sender.getName() + ": " + content);
            } catch (Exception e) {
                // ignore
            }
        }

        return savedMessage;
    }

    @Override
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

            // FCM Push Notification
            if (client.getFcmToken() != null && !client.getFcmToken().isEmpty()) {
                try {
                    fcmService.sendPushNotification(client.getFcmToken(), "Diyetisyeninizden Duyuru", content);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return savedMessage;
    }

    @Override
    public List<ConversationSummary> getInbox(User currentUser) {
        if (currentUser.getRole() == Role.ROLE_USER) {
            List<ConversationSummary> inbox = new ArrayList<>();
            User dietitian = currentUser.getDietitian();
            if (dietitian != null) {
                List<Message> lastMsgs = messageRepository.findLastPrivateMessage(currentUser, dietitian);
                String lastMsgText = null;
                LocalDateTime lastMsgTime = null;
                if (!lastMsgs.isEmpty()) {
                    Message last = lastMsgs.get(0);
                    lastMsgText = last.getContent();
                    lastMsgTime = last.getSentAt();
                }

                long unreadCount = messageRepository.countUnreadMessages(dietitian, currentUser);

                inbox.add(ConversationSummary.builder()
                        .partnerId(dietitian.getId())
                        .partnerName(dietitian.getName())
                        .partnerEmail(dietitian.getEmail())
                        .partnerCategory(null)
                        .lastMessage(lastMsgText)
                        .lastMessageSentAt(lastMsgTime)
                        .unreadCount(unreadCount)
                        .build());
            }
            return inbox;
        }

        if (currentUser.getRole() != Role.ROLE_DIETITIAN) {
            throw new IllegalArgumentException("Sadece diyetisyenler ve danışanlar gelen kutusunu görebilir.");
        }

        List<User> clients = userRepository.findByDietitianIdAndRole(currentUser.getId(), Role.ROLE_USER);
        List<ConversationSummary> inbox = new ArrayList<>();

        for (User client : clients) {
            List<Message> lastMsgs = messageRepository.findLastPrivateMessage(currentUser, client);
            String lastMsgText = null;
            LocalDateTime lastMsgTime = null;
            if (!lastMsgs.isEmpty()) {
                Message last = lastMsgs.get(0);
                lastMsgText = last.getContent();
                lastMsgTime = last.getSentAt();
            }

            long unreadCount = messageRepository.countUnreadMessages(client, currentUser);

            inbox.add(ConversationSummary.builder()
                    .partnerId(client.getId())
                    .partnerName(client.getName())
                    .partnerEmail(client.getEmail())
                    .partnerCategory(client.getCategory() != null ? client.getCategory().name() : null)
                    .lastMessage(lastMsgText)
                    .lastMessageSentAt(lastMsgTime)
                    .unreadCount(unreadCount)
                    .build());
        }

        // Sort by last message date descending, placing conversations with messages first
        inbox.sort((c1, c2) -> {
            if (c1.getLastMessageSentAt() == null && c2.getLastMessageSentAt() == null) {
                return 0;
            }
            if (c1.getLastMessageSentAt() == null) {
                return 1;
            }
            if (c2.getLastMessageSentAt() == null) {
                return -1;
            }
            return c2.getLastMessageSentAt().compareTo(c1.getLastMessageSentAt());
        });

        return inbox;
    }
}
