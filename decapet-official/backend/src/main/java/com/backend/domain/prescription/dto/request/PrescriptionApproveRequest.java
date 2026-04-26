package com.backend.domain.prescription.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "처방전 승인 요청")
public record PrescriptionApproveRequest(
        @Schema(description = "결제 금액", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "결제 금액을 입력해주세요.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal price
) {
}
