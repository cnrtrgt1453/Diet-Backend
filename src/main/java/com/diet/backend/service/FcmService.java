package com.diet.backend.service;

/**
 * ISP: Firebase Cloud Messaging push bildirim arayüzü.
 */
public interface FcmService {

    /**
     * Firebase SDK'sını başlatır (PostConstruct).
     */
    void initialize();

    /**
     * Belirtilen FCM token'a push bildirim gönderir.
     */
    void sendPushNotification(String fcmToken, String title, String body);
}
