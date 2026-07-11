package com.diet.backend.repository;

import com.diet.backend.model.ClientDietitianHistory;
import com.diet.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientDietitianHistoryRepository extends JpaRepository<ClientDietitianHistory, Long> {

    /**
     * Belirli bir danışanın tüm diyetisyen değişiklik geçmişini döner.
     */
    List<ClientDietitianHistory> findByClientOrderByChangedAtDesc(User client);

    /**
     * Belirli bir diyetisyenin tüm eski danışan geçmişini döner.
     */
    List<ClientDietitianHistory> findByPreviousDietitianOrderByChangedAtDesc(User previousDietitian);
}
