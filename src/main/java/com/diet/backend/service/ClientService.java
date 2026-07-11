package com.diet.backend.service;

import com.diet.backend.model.User;
import java.util.List;

public interface ClientService {
    List<User> getClients(User dietitian);
    User addClient(User clientRequest, User dietitian);
    User updateClient(Long clientId, User clientRequest, User dietitian);
    void deleteClient(Long clientId, User dietitian);
}
