package com.backend.domain.schedule.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.schedule.dto.request.CreateScheduleRequest;
import com.backend.domain.schedule.dto.request.TimeSlotRequest;
import com.backend.domain.schedule.entity.Schedule;
import com.backend.domain.schedule.entity.TimeSlot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {

    private final TimeSlotMapper timeSlotMapper;

    public Schedule toEntity(CreateScheduleRequest request) {
        Schedule schedule = Schedule.builder()
                .scheduleDate(request.date())
                .build();

        for (TimeSlotRequest slotRequest : request.timeSlots()) {
            TimeSlot timeSlot = timeSlotMapper.toEntity(slotRequest);
            schedule.addTimeSlot(timeSlot);
        }

        return schedule;
    }
}
