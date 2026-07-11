package com.diet.backend.service.impl;

import com.diet.backend.service.FcmService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class FcmServiceImpl implements FcmService {

    @Value("${app.firebase.config-path:}")
    private String firebaseConfigPath;

    private boolean initialized = false;

    @Override
    @PostConstruct
    public void initialize() {
        try {
            if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                initialized = true;
                log.info("Firebase Admin SDK successfully initialized from path: {}", firebaseConfigPath);
            } else {
                log.warn("Firebase config path not specified. FCM will run in MOCK/DRY-RUN mode.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK. FCM will run in MOCK/DRY-RUN mode.", e);
        }
    }

    @Override
    public void sendPushNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("Cannot send FCM message: target FCM token is null or empty.");
            return;
        }

        if (!initialized) {
            log.info("[MOCK FCM PUSH] Token: {}, Title: {}, Body: {}", fcmToken, title, body);
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM message to token {}", fcmToken, e);
        }
    }
}
