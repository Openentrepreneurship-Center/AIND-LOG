package com.backend.domain.product.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "상품 수정 요청", example = """
        {
          "name": "프리미엄 강아지 사료 (수정)",
          "description": "업데이트된 설명입니다.",
          "basePrice": 55000,
          "price": 49500,
          "weight": 3.0,
          "stockQuantity": 80,
          "expirationDate": "2025-12-31",
          "allowMultiple": true,
          "additionalInfo": ["원산지: 한국", "제조사: 대웅"]
        }""")
public record UpdateProductRequest(
        @Schema(description = "상품명 (필수)", example = "프리미엄 강아지 사료", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "상품명을 입력해주세요.")
        String name,

        @Schema(description = "상품 설명 (선택)", example = "모든 연령대의 강아지에게 적합한 프리미엄 사료입니다.")
        String description,

        @Schema(description = "원가 (필수)", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "원가를 입력해주세요.")
        @Positive(message = "원가는 0보다 커야 합니다.")
        BigDecimal basePrice,

        @Schema(description = "판매가 (필수)", example = "45000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "판매가를 입력해주세요.")
        @Positive(message = "판매가는 0보다 커야 합니다.")
        BigDecimal price,

        @Schema(description = "무게 (kg, 선택)", example = "3.0")
        BigDecimal weight,

        @Schema(description = "재고 수량 (필수)", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "재고 수량을 입력해주세요.")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        @Schema(description = "유통기한 (선택)", example = "2025-12-31")
        LocalDate expirationDate,

        @Schema(description = "대량구매 가능 여부 (선택, 기본값: true)", example = "true")
        Boolean allowMultiple,

        @Schema(description = "기타 상품 정보 (선택)", example = "[\"원산지: 한국\", \"제조사: 대웅\"]")
        List<String> additionalInfo
) {
}
