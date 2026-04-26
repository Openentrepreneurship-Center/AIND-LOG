package com.backend.domain.medicine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.backend.domain.medicine.entity.MedicineType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의약품 상세 응답 (사용자용)")
public record MedicineDetailResponse(
        @Schema(description = "의약품 ID", example = "507f1f77bcf86cd799439011")
        String id,

        @Schema(description = "의약품명", example = "넥스가드 스펙트라")
        String name,

        @Schema(description = "의약품 설명", example = "개에 대한 벼룩, 진드기 및 내부 기생충 예방 및 치료")
        String description,

        @Schema(description = "가격", example = "35000")
        BigDecimal price,

        @Schema(description = "판매가", example = "30000")
        BigDecimal salePrice,

        @Schema(description = "의약품 타입 (PARASITE: 기생충예방, SKIN: 피부, COGNITIVE: 인지기능, OTHER: 기타)", example = "PARASITE")
        MedicineType type,

        @Schema(description = "유통기한", example = "2025-12-31")
        LocalDate expirationDate,

        @Schema(description = "의약품 이미지 URL", example = "https://s3.amazonaws.com/bucket/medicine1.jpg")
        String imageUrl,

        @Schema(description = "1개당 처방 개월 수 (기생충약 타입일 때만 사용, 예: 3이면 1개=3개월)", example = "3")
        Integer monthsPerUnit,

        @Schema(description = "처방 문진표 목록")
        List<QuestionnaireItemResponse> questionnaire
) {
}
