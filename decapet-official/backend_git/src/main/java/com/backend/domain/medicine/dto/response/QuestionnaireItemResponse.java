package com.backend.domain.medicine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "처방 설문지 항목")
public record QuestionnaireItemResponse(
        @Schema(description = "질문 ID", example = "q1")
        String questionId,

        @Schema(description = "질문 내용", example = "반려동물의 현재 건강 상태는 어떠신가요?")
        String question
) {
}
