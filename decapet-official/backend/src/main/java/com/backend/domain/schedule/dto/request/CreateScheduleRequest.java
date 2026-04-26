package com.backend.domain.schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일정 생성 요청")
public record CreateScheduleRequest(
        @Schema(description = "일정 날짜", example = "2025-12-20", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "일정 날짜를 입력해주세요.")
        @FutureOrPresent(message = "과거 날짜에는 일정을 생성할 수 없습니다.")
        LocalDate date,

        @Schema(description = "시간대 목록 (최소 1개 이상)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "시간대를 하나 이상 입력해주세요.")
        @Valid
        List<TimeSlotRequest> timeSlots
) {
}
