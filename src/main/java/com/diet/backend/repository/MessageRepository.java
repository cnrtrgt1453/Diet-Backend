package com.diet.backend.repository;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END " +
           "FROM Message m WHERE (m.sender.id = :userId OR m.recipient.id = :userId) AND m.isBroadcast = false")
    List<Long> findChatPartners(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.recipient = :user2 AND m.isBroadcast = false) OR " +
           "(m.sender = :user2 AND m.recipient = :user1 AND m.isBroadcast = false) OR " +
           "(m.sender = :dietitian AND m.isBroadcast = true) " +
           "ORDER BY m.sentAt ASC")
    List<Message> findChatHistory(
        @Param("user1") User user1, 
        @Param("user2") User user2, 
        @Param("dietitian") User dietitian
    );

    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1)) " +
           "AND m.isBroadcast = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLastPrivateMessage(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.sender = :sender AND m.recipient = :recipient AND m.isRead = false AND m.isBroadcast = false")
    long countUnreadMessages(@Param("sender") User sender, @Param("recipient") User recipient);
}
