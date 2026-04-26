package com.backend.domain.appointment.dto.internal;

import java.util.List;

import com.backend.domain.appointment.entity.AppointmentStatus;

public record AppointmentSearchCondition(
        String userId,
        String petId,
        String timeSlotId,
        List<AppointmentStatus> statuses
) {
}
