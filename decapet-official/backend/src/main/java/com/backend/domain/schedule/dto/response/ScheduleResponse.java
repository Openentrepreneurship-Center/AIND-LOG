package com.backend.domain.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 응답")
public record ScheduleResponse(
        @Schema(description = "일정 ID", example = "schedule123")
        String id,

        @Schema(description = "일정 날짜", example = "2025-12-20")
        LocalDate date,

        @Schema(description = "시간대 목록")
        List<TimeSlotResponse> timeSlots
) {
}
