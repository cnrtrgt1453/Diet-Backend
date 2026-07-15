package com.diet.backend.repository;

import com.diet.backend.model.User;
import com.diet.backend.model.Role;
import com.diet.backend.model.ClientCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Diyetisyene bağlı danışanları getirmek için (Başvurusu olan adayları filtreler)
    @Query("SELECT u FROM User u WHERE u.dietitian.id = :dietitianId AND u.role = :role AND u.dietitianApplicationStatus IS NULL")
    List<User> findByDietitianIdAndRole(@Param("dietitianId") Long dietitianId, @Param("role") Role role);
    
    // İlgili kategorilerdeki danışan sayıları için (Başvurusu olan adayları filtreler)
    @Query("SELECT COUNT(u) FROM User u WHERE u.dietitian.id = :dietitianId AND u.category = :category AND u.dietitianApplicationStatus IS NULL")
    long countByDietitianIdAndCategory(@Param("dietitianId") Long dietitianId, @Param("category") ClientCategory category);
    
    // Toplam danışan sayısı için (Başvurusu olan adayları filtreler)
    @Query("SELECT COUNT(u) FROM User u WHERE u.dietitian.id = :dietitianId AND u.role = :role AND u.dietitianApplicationStatus IS NULL")
    long countByDietitianIdAndRole(@Param("dietitianId") Long dietitianId, @Param("role") Role role);
}
