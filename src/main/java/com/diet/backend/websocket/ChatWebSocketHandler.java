package com.diet.backend.websocket;

import com.diet.backend.model.Message;
import com.diet.backend.model.User;
import com.diet.backend.security.JwtTokenProvider;
import com.diet.backend.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    // Maps User ID -> List of WebSocket Sessions (in case of multiple connections/tabs)
    private final ConcurrentHashMap<Long, List<WebSocketSession>> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        String token = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1 && "token".equals(pair[0])) {
                    token = pair[1];
                    break;
                }
            }
        }

        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromJWT(token);
            try {
                User user = (User) userDetailsService.loadUserByUsername(username);
                session.getAttributes().put("user", user);

                activeSessions.computeIfAbsent(user.getId(), k -> new CopyOnWriteArrayList<>()).add(session);
                log.info("WebSocket connection established for user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to load user during WebSocket handshake", e);
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            log.warn("Invalid token for WebSocket connection request");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User sender = (User) session.getAttributes().get("user");
        if (sender == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        try {
            ChatMessagePayload payload = objectMapper.readValue(message.getPayload(), ChatMessagePayload.class);
            if (payload.isBroadcast()) {
                // Broadcast messages from dietitian
                Message savedMessage = messageService.sendBroadcastMessage(sender, payload.getContent());
                broadcastMessageToClients(sender, savedMessage);
            } else {
                // Private message between dietitian and client
                Message savedMessage = messageService.sendPrivateMessage(sender, payload.getRecipientId(), payload.getContent());
                sendPrivateMessage(sender.getId(), payload.getRecipientId(), savedMessage);
            }
        } catch (Exception e) {
            log.error("Failed to process WebSocket message from user {}", sender.getEmail(), e);
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user != null) {
            List<WebSocketSession> sessions = activeSessions.get(user.getId());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    activeSessions.remove(user.getId());
                }
            }
            log.info("WebSocket connection closed for user: {}", user.getEmail());
        }
    }

    private void sendPrivateMessage(Long senderId, Long recipientId, Message savedMessage) {
        String payload = serializeMessage(savedMessage);
        if (payload == null) return;

        // Send to recipient
        sendToUser(recipientId, payload);
        // Send back to sender to confirm delivery / sync multiple devices
        sendToUser(senderId, payload);
    }

    private void broadcastMessageToClients(User dietitian, Message savedMessage) {
        String payload = serializeMessage(savedMessage);
        if (payload == null) return;

        // Push to active client socket sessions of this dietitian
        activeSessions.forEach((userId, sessions) -> {
            if (sessions.isEmpty()) return;
            WebSocketSession firstSession = sessions.get(0);
            User user = (User) firstSession.getAttributes().get("user");
            if (user != null && user.getDietitian() != null && user.getDietitian().getId().equals(dietitian.getId())) {
                sendToUser(userId, payload);
            }
        });

        // Also send to dietitian's own sessions
        sendToUser(dietitian.getId(), payload);
    }

    private void sendToUser(Long userId, String payload) {
        List<WebSocketSession> sessions = activeSessions.get(userId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        log.error("Failed to send message to user session {}", userId, e);
                    }
                }
            }
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> errorMap = Map.of(
                    "type", "error",
                    "message", errorMessage
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMap)));
        } catch (IOException e) {
            log.error("Failed to send error message to session", e);
        }
    }

    private String serializeMessage(Message message) {
        try {
            Map<String, Object> responseMap = Map.of(
                    "type", "message",
                    "data", message
            );
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
            return null;
        }
    }

    @Data
    public static class ChatMessagePayload {
        private Long recipientId;
        private String content;
        private boolean isBroadcast;
    }
}
