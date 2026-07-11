package com.diet.backend.service.impl;

import com.diet.backend.model.*;
import com.diet.backend.repository.ClientDietitianHistoryRepository;
import com.diet.backend.repository.DietitianConnectionRequestRepository;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.repository.NotificationRepository;
import com.diet.backend.service.DietitianConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietitianConnectionServiceImpl implements DietitianConnectionService {

    private final DietitianConnectionRequestRepository connectionRequestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ClientDietitianHistoryRepository historyRepository;

    @Override
    public List<User> getAllDietitians() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_DIETITIAN)
                .collect(Collectors.toList());
    }

    @Override
    public List<DietitianConnectionRequest> getClientRequests(User client) {
        return connectionRequestRepository.findByClient(client);
    }

    @Override
    @Transactional
    public DietitianConnectionRequest sendConnectionRequest(User client, Long dietitianId) {
        User dietitian = userRepository.findById(dietitianId)
                .orElseThrow(() -> new RuntimeException("Diyetisyen bulunamadı"));

        if (dietitian.getRole() != Role.ROLE_DIETITIAN) {
            throw new RuntimeException("Seçilen kullanıcı bir diyetisyen değil");
        }

        // Check if there is already a pending connection request
        connectionRequestRepository.findByClientAndDietitianAndStatus(client, dietitian, ConnectionStatus.PENDING)
                .ifPresent(r -> {
                    throw new RuntimeException("Bu diyetisyene zaten bekleyen bir talebiniz var");
                });

        DietitianConnectionRequest request = DietitianConnectionRequest.builder()
                .client(client)
                .dietitian(dietitian)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        DietitianConnectionRequest savedRequest = connectionRequestRepository.save(request);

        // Send notification to dietitian
        Notification notification = Notification.builder()
                .recipient(dietitian)
                .title("Yeni Çalışma Talebi")
                .message(client.getName() + " size çalışma talebi gönderdi.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        return savedRequest;
    }

    @Override
    public List<DietitianConnectionRequest> getPendingRequestsForDietitian(User dietitian) {
        return connectionRequestRepository.findByDietitianAndStatus(dietitian, ConnectionStatus.PENDING);
    }

    @Override
    @Transactional
    public void approveRequest(User dietitian, Long requestId) {
        DietitianConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Talep bulunamadı"));

        if (!request.getDietitian().getId().equals(dietitian.getId())) {
            throw new RuntimeException("Bu talebi onaylama yetkiniz yok");
        }

        request.setStatus(ConnectionStatus.APPROVED);
        connectionRequestRepository.save(request);

        User client = request.getClient();
        User previousDietitian = client.getDietitian();

        // Soft-Delete: Eski diyetisyen ilişkisini tarihçeye kaydet
        if (previousDietitian != null) {
            ClientDietitianHistory history = ClientDietitianHistory.builder()
                    .client(client)
                    .previousDietitian(previousDietitian)
                    .newDietitian(dietitian)
                    .changedAt(LocalDateTime.now())
                    .reason("CONNECTION_APPROVED")
                    .build();
            historyRepository.save(history);
        }

        client.setDietitian(dietitian);
        userRepository.save(client);

        // Send notification to client
        Notification notification = Notification.builder()
                .recipient(client)
                .title("Talebiniz Kabul Edildi")
                .message("Diyetisyeniniz " + dietitian.getName() + " çalışma talebinizi onayladı!")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void rejectRequest(User dietitian, Long requestId) {
        DietitianConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Talep bulunamadı"));

        if (!request.getDietitian().getId().equals(dietitian.getId())) {
            throw new RuntimeException("Bu talebi reddetme yetkiniz yok");
        }

        request.setStatus(ConnectionStatus.REJECTED);
        connectionRequestRepository.save(request);

        // Send notification to client
        Notification notification = Notification.builder()
                .recipient(request.getClient())
                .title("Talebiniz Reddedildi")
                .message("Diyetisyeniniz " + dietitian.getName() + " çalışma talebinizi reddetti.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
