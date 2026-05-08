package com.backend.domain.medicinecart.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMedicineRequest(
        @NotBlank(message = "반려동물 ID를 입력해주세요.")
        String petId,

        @NotBlank(message = "의약품 ID를 입력해주세요.")
        String medicineId,

        @NotBlank(message = "예약 시간대 ID를 입력해주세요.")
        String timeSlotId,

        @NotNull(message = "수량을 입력해주세요.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity,

        @Valid
        List<QuestionnaireAnswerRequest> questionnaireAnswers
) {
}
