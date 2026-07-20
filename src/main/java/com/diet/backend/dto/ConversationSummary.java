package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummary {
    private Long partnerId;
    private String partnerName;
    private String partnerEmail;
    private String partnerCategory;
    private String lastMessage;
    private LocalDateTime lastMessageSentAt;
    private long unreadCount;
    private String profilePictureUrl;
}
