package com.backend.domain.customproduct.dto.request;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "커스텀 상품 신청 요청", example = """
        {
          "petId": "01HXYZ...",
          "name": "강아지 맞춤 사료",
          "description": "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다.",
          "requestedPrice": 50000,
          "weight": 2.5,
          "quantity": 3,
          "additionalInfo": ["원산지: 한국", "성분: 유기농"]
        }""")
public record CreateCustomProductRequest(
        @Schema(description = "반려동물 ID", example = "01HXYZ...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "반려동물을 선택해주세요.")
        String petId,

        @Schema(description = "상품명", example = "강아지 맞춤 사료", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "상품명을 입력해주세요.")
        String name,

        @Schema(description = "상품 설명", example = "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다. 알레르기가 있어서 일반 사료를 먹을 수 없습니다.")
        String description,

        @Schema(description = "희망 가격", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "희망 가격을 입력해주세요.")
        @Positive(message = "가격은 양수여야 합니다.")
        BigDecimal requestedPrice,

        @Schema(description = "상품 무게(kg)", example = "2.5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "무게를 입력해주세요.")
        @Positive(message = "무게는 양수여야 합니다.")
        BigDecimal weight,

        @Schema(description = "상품 개수 (1세트당 개수)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상품 개수를 입력해주세요.")
        @Positive(message = "상품 개수는 양수여야 합니다.")
        Integer quantity,

        @Schema(description = "기타 상품 정보 (선택)", example = "[\"원산지: 한국\", \"성분: 유기농\"]")
        List<String> additionalInfo
) {
}
