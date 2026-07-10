package com.diet.backend.repository;

import com.diet.backend.model.DietitianConnectionRequest;
import com.diet.backend.model.User;
import com.diet.backend.model.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DietitianConnectionRequestRepository extends JpaRepository<DietitianConnectionRequest, Long> {
    List<DietitianConnectionRequest> findByDietitianAndStatus(User dietitian, ConnectionStatus status);
    List<DietitianConnectionRequest> findByClient(User client);
    Optional<DietitianConnectionRequest> findByClientAndDietitianAndStatus(User client, User dietitian, ConnectionStatus status);
}
