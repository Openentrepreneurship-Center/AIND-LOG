package com.backend.domain.appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "예약 생성 요청")
public record CreateAppointmentRequest(
        @Schema(description = "반려동물 ID", example = "pet123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "반려동물 ID를 입력해주세요.")
        String petId,

        @Schema(description = "예약하려는 시간대 ID", example = "slot123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "예약 시간대 ID를 입력해주세요.")
        String timeSlotId
) {
}
