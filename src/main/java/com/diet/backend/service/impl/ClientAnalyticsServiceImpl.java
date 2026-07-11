package com.diet.backend.service.impl;

import com.diet.backend.model.ClientCategory;
import com.diet.backend.model.Role;
import com.diet.backend.model.User;
import com.diet.backend.repository.UserRepository;
import com.diet.backend.service.ClientAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAnalyticsServiceImpl implements ClientAnalyticsService {

    private final UserRepository userRepository;

    @Override
    public Map<String, Object> getStats(User dietitian) {
        Map<String, Object> stats = new HashMap<>();
        
        // Total clients count
        stats.put("total", userRepository.countByDietitianIdAndRole(dietitian.getId(), Role.ROLE_USER));
        
        // Count dynamically for all categories to comply with Open-Closed Principle (OCP)
        for (ClientCategory category : ClientCategory.values()) {
            String key = convertToCamelCase(category.name());
            stats.put(key, userRepository.countByDietitianIdAndCategory(dietitian.getId(), category));
        }
        
        return stats;
    }

    private String convertToCamelCase(String enumName) {
        if ("GLP_1".equals(enumName)) {
            return "glp1";
        }
        String[] parts = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1));
        }
        return sb.toString();
    }
}
