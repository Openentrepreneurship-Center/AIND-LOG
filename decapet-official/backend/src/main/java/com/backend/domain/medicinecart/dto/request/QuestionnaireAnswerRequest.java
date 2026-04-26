package com.backend.domain.medicinecart.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record QuestionnaireAnswerRequest(
        @NotBlank(message = "질문 ID를 입력해주세요.")
        String questionId,

        @AssertTrue(message = "문진 항목에 동의해야 합니다.")
        boolean answer
) {
}
