package com.diet.backend.event;

import com.diet.backend.model.DailyLog;
import lombok.Getter;

@Getter
public class DailyLogSubmittedEvent {
    private final DailyLog dailyLog;

    public DailyLogSubmittedEvent(DailyLog dailyLog) {
        this.dailyLog = dailyLog;
    }
}
