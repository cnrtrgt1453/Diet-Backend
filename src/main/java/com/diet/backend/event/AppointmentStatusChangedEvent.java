package com.diet.backend.event;

import com.diet.backend.model.Appointment;
import lombok.Getter;

@Getter
public class AppointmentStatusChangedEvent {
    private final Appointment appointment;

    public AppointmentStatusChangedEvent(Appointment appointment) {
        this.appointment = appointment;
    }
}
