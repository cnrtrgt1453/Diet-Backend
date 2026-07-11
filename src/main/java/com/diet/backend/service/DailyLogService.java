package com.diet.backend.service;

import com.diet.backend.model.DailyLog;
import com.diet.backend.model.User;
import java.time.LocalDate;
import java.util.List;

public interface DailyLogService {
    DailyLog saveDailyLog(DailyLog logRequest, User client);
    List<DailyLog> getMyDailyLogs(LocalDate startDate, LocalDate endDate, User client);
    List<DailyLog> getClientDailyLogs(Long clientId, LocalDate startDate, LocalDate endDate, User dietitian);
}
