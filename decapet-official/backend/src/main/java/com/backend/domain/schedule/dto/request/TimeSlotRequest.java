package com.backend.domain.schedule.dto.request;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "시간대 요청")
public record TimeSlotRequest(
        @Schema(description = "예약 시간", example = "14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "시간을 입력해주세요.")
        LocalTime time,

        @Schema(description = "예약 장소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "장소를 입력해주세요.")
        String location,

        @Schema(description = "최대 예약 가능 인원", example = "1", defaultValue = "1")
        Integer maxCapacity
) {
    public int resolvedMaxCapacity() {
        return maxCapacity != null && maxCapacity > 0 ? maxCapacity : 1;
    }
}
