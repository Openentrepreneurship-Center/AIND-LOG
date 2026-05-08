package com.backend.domain.schedule.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.schedule.dto.request.TimeSlotRequest;
import com.backend.domain.schedule.entity.TimeSlot;

@Component
public class TimeSlotMapper {

    public TimeSlot toEntity(TimeSlotRequest request) {
        return TimeSlot.builder()
                .time(request.time())
                .location(request.location())
                .maxCapacity(request.resolvedMaxCapacity())
                .build();
    }
}
