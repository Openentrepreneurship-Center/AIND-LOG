package com.backend.domain.schedule.dto.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.schedule.dto.response.ScheduleResponse;
import com.backend.domain.schedule.dto.response.TimeSlotResponse;
import com.backend.domain.schedule.entity.Schedule;
import com.backend.domain.schedule.entity.TimeSlot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleResponseMapper {

    private final TimeSlotResponseMapper timeSlotResponseMapper;

    public ScheduleResponse toResponse(Schedule schedule) {
        List<TimeSlotResponse> slots = schedule.getTimeSlots().stream()
                .sorted(Comparator.comparing(TimeSlot::getTime))
                .map(timeSlotResponseMapper::toResponse)
                .toList();

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getScheduleDate(),
                slots
        );
    }
}
