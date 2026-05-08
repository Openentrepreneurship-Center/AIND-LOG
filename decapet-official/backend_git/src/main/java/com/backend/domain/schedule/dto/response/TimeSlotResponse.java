package com.backend.domain.schedule.dto.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시간대 응답")
public record TimeSlotResponse(
        @Schema(description = "시간대 ID", example = "slot123")
        String id,

        @Schema(description = "예약 시간", example = "14:00:00")
        LocalTime time,

        @Schema(description = "예약 장소", example = "서울시 강남구 테헤란로 123")
        String location,

        @Schema(description = "최대 인원", example = "20")
        int maxCapacity,

        @Schema(description = "현재 예약 수", example = "5")
        int currentCount,

        @Schema(description = "잔여 인원", example = "15")
        int remainingCapacity
) {
}
