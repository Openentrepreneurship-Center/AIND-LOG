package com.backend.domain.appointment.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.dto.internal.CreateAppointmentCommand;
import com.backend.domain.appointment.entity.Appointment;

@Component
public class AppointmentMapper {

    public Appointment toEntity(CreateAppointmentCommand command) {
        return Appointment.builder()
                .user(command.user())
                .pet(command.pet())
                .timeSlot(command.timeSlot())
                .build();
    }
}
