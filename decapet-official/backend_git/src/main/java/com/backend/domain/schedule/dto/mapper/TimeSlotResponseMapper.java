package com.backend.domain.schedule.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.repository.AppointmentRepository;
import com.backend.domain.schedule.dto.response.TimeSlotResponse;
import com.backend.domain.schedule.entity.TimeSlot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeSlotResponseMapper {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED);

    private final AppointmentRepository appointmentRepository;

    public TimeSlotResponse toResponse(TimeSlot slot) {
        int currentCount = (int) appointmentRepository.countDistinctUserByTimeSlotIdAndStatusIn(
                slot.getId(), ACTIVE_STATUSES);
        int maxCapacity = slot.getMaxCapacity();
        int remaining = Math.max(0, maxCapacity - currentCount);

        return new TimeSlotResponse(
                slot.getId(),
                slot.getTime(),
                slot.getLocation(),
                maxCapacity,
                currentCount,
                remaining
        );
    }
}
