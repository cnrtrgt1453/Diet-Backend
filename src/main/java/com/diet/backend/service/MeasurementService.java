package com.diet.backend.service;

import com.diet.backend.model.Measurement;
import com.diet.backend.model.User;
import java.util.List;

public interface MeasurementService {
    List<Measurement> getClientMeasurements(Long clientId, User loggedInUser);
    Measurement addMeasurement(Long clientId, Measurement measurementRequest, User dietitian);
}
