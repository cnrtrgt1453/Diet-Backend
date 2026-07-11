package com.diet.backend.service;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;

import java.util.List;

/**
 * ISP: Mesajlaşma sorumluluğu arayüzü.
 * Controller sınıfları somut sınıf yerine bu interface'e bağımlıdır.
 */
public interface MessageService {

    List<Message> getChatHistory(User currentUser, Long otherUserId);

    Message sendPrivateMessage(User sender, Long recipientId, String content);

    Message sendBroadcastMessage(User dietitian, String content);
}
