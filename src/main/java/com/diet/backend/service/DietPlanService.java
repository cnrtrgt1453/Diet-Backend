package com.diet.backend.service;

import com.diet.backend.model.DietPlan;
import com.diet.backend.model.User;
import java.util.List;

public interface DietPlanService {
    List<DietPlan> getClientDiets(Long clientId, User dietitian);
    DietPlan addDietPlan(Long clientId, DietPlan dietPlanRequest, User dietitian);
}
