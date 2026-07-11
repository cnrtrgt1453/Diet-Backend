package com.diet.backend.service;

import com.diet.backend.model.User;
import java.util.Map;

public interface ClientAnalyticsService {
    Map<String, Object> getStats(User dietitian);
}
