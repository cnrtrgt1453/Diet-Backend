package com.diet.backend.service;

import com.diet.backend.dto.*;
import com.diet.backend.model.User;
import java.util.List;

public interface AdvancedAnalyticsService {
    List<CohortDto> getCohortAnalysis(User dietitian);
    List<CategoryComplianceDto> getCategoryCompliance(User dietitian);
    List<ClientWeightLossRateDto> getClientWeightLossRates(User dietitian);
    CorrelationAnalysisDto getCorrelationAnalysis(Long clientId, User loggedInUser);
    WeightPredictionDto getWeightPrediction(Long clientId, User loggedInUser);
}
