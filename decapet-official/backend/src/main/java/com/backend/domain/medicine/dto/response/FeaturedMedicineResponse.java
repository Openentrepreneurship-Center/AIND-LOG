package com.backend.domain.medicine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 의약품 응답 (메인 페이지용)")
public record FeaturedMedicineResponse(
        @Schema(description = "의약품 ID", example = "507f1f77bcf86cd799439011")
        String id,

        @Schema(description = "의약품명", example = "넥스가드 스펙트라")
        String name,

        @Schema(description = "가격", example = "35000")
        BigDecimal price,

        @Schema(description = "유통기한", example = "2025-12-31")
        LocalDate expirationDate,

        @Schema(description = "의약품 이미지 URL", example = "https://s3.amazonaws.com/bucket/medicine1.jpg")
        String imageUrl,

        @Schema(description = "판매가 (할인가)", example = "29000")
        BigDecimal salePrice
) {
}
