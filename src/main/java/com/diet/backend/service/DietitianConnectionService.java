package com.diet.backend.service;

import com.diet.backend.model.DietitianConnectionRequest;
import com.diet.backend.model.User;

import java.util.List;

/**
 * ISP: Diyetisyen bağlantı talebi yönetimi arayüzü.
 */
public interface DietitianConnectionService {

    List<User> getAllDietitians();

    List<DietitianConnectionRequest> getClientRequests(User client);

    DietitianConnectionRequest sendConnectionRequest(User client, Long dietitianId);

    List<DietitianConnectionRequest> getPendingRequestsForDietitian(User dietitian);

    void approveRequest(User dietitian, Long requestId);

    void rejectRequest(User dietitian, Long requestId);
}
