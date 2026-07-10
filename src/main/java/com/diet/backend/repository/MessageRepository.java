package com.diet.backend.repository;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
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
}
