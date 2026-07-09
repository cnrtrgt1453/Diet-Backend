package com.diet.backend.event;

import com.diet.backend.model.DietPlan;
import lombok.Getter;

@Getter
public class DietPlanAssignedEvent {
    private final DietPlan dietPlan;

    public DietPlanAssignedEvent(DietPlan dietPlan) {
        this.dietPlan = dietPlan;
    }
}
