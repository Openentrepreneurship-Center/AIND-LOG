package com.backend.domain.appointment.dto.internal;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.user.entity.User;

public record CreateAppointmentCommand(
        User user,
        Pet pet,
        TimeSlot timeSlot
) {
}
