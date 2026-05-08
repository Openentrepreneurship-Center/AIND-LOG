package com.backend.domain.customproduct.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "커스텀 상품 승인 요청")
public record ApproveCustomProductRequest(
        @Schema(description = "승인 가격 (관리자가 결정한 최종 가격)", example = "55000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "승인 가격을 입력해주세요.")
        @Positive(message = "가격은 양수여야 합니다.")
        BigDecimal approvedPrice,

        @Schema(description = "수량 추가 가능 여부 (장바구니에서 수량 증가 허용 여부)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "수량 추가 가능 여부를 선택해주세요.")
        Boolean allowMultiple,

        @Schema(description = "재고 수량", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "재고 수량을 입력해주세요.")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity
) {
}
