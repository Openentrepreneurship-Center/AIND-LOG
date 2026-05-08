package com.backend.domain.customproduct.dto.request;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "커스텀 상품 수정 요청")
public record UpdateCustomProductRequest(
        @Schema(description = "상품명", example = "강아지 맞춤 사료", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "상품명을 입력해주세요.")
        String name,

        @Schema(description = "상품 설명")
        String description,

        @Schema(description = "승인 가격 (판매가)", example = "55000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "승인 가격을 입력해주세요.")
        @Positive(message = "가격은 양수여야 합니다.")
        BigDecimal approvedPrice,

        @Schema(description = "정가", example = "60000")
        @Positive(message = "정가는 양수여야 합니다.")
        BigDecimal basePrice,

        @Schema(description = "상품 무게(kg)", example = "2.5")
        @Positive(message = "무게는 양수여야 합니다.")
        BigDecimal weight,

        @Schema(description = "상품 개수 (1세트당 개수)", example = "3")
        @Positive(message = "상품 개수는 양수여야 합니다.")
        Integer quantity,

        @Schema(description = "대량구매 가능 여부", example = "true")
        Boolean allowMultiple,

        @Schema(description = "기타 상품 정보", example = "[\"원산지: 한국\", \"성분: 유기농\"]")
        List<String> additionalInfo,

        @Schema(description = "재고 수량", example = "10")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity
) {
}
