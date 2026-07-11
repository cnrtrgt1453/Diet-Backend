package com.diet.backend.service;

import com.diet.backend.dto.DietitianApplicationDto;
import com.diet.backend.model.DietitianApplication;
import com.diet.backend.model.User;

import java.util.List;

/**
 * ISP: Diyetisyen başvuru yönetimi arayüzü.
 */
public interface DietitianApplicationService {

    DietitianApplication apply(DietitianApplicationDto dto);

    List<DietitianApplication> getPendingApplications(User adminUser);

    DietitianApplication startReview(Long id, User adminUser);

    DietitianApplication approveApplication(Long id, User adminUser);

    DietitianApplication rejectApplication(Long id, String reason, User adminUser);
}
