package com.backend.domain.prescription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "처방전 거절 요청")
public record PrescriptionRejectRequest(
        @Schema(description = "거절 사유 (사용자에게 전달됨)", example = "처방전 이미지가 불명확합니다. 다시 촬영하여 업로드해주세요.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "거절 사유를 입력해주세요.")
        String comment
) {
}
