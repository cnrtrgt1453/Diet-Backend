package com.diet.backend.repository;

import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.model.ClientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Diyetisyene bağlı danışanları getirmek için
    List<User> findByDietitianIdAndRole(Long dietitianId, Role role);
    
    // İlgili kategorilerdeki danışan sayıları için
    long countByDietitianIdAndCategory(Long dietitianId, ClientCategory category);
    
    // Toplam danışan sayısı için
    long countByDietitianIdAndRole(Long dietitianId, Role role);
}
