package com.backend.domain.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 상세 응답 (사용자용)")
public record ProductDetailResponse(
        @Schema(description = "상품 ID", example = "507f1f77bcf86cd799439011")
        String id,

        @Schema(description = "상품명", example = "프리미엄 강아지 사료")
        String name,

        @Schema(description = "상품 설명", example = "모든 연령대의 강아지에게 적합한 프리미엄 사료입니다.")
        String description,

        @Schema(description = "원가", example = "50000")
        BigDecimal basePrice,

        @Schema(description = "판매가", example = "45000")
        BigDecimal price,

        @Schema(description = "무게 (kg)", example = "3.0")
        BigDecimal weight,

        @Schema(description = "유통기한", example = "2025-12-31")
        LocalDate expirationDate,

        @Schema(description = "상품 이미지 URL", example = "https://s3.amazonaws.com/bucket/product1.jpg")
        String imageUrl,

        @Schema(description = "대량구매 가능 여부", example = "true")
        Boolean allowMultiple,

        @Schema(description = "기타 상품 정보", example = "[\"원산지: 한국\", \"제조사: 대웅\"]")
        List<String> additionalInfo
) {
}
