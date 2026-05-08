package com.backend.domain.prescription.dto.request;

import com.backend.domain.prescription.entity.PrescriptionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "처방전 생성 요청")
public record CreatePrescriptionRequest(
        @Schema(description = "반려동물 ID", example = "pet123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "반려동물 ID를 입력해주세요.")
        String petId,

        @Schema(description = "신청 유형 (SIMPLE: 간단 신청, DETAILED: 상세 신청)", example = "DETAILED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "신청 유형을 선택해주세요.")
        PrescriptionType type,

        // 간단 신청용 필드
        @Schema(description = "간단 설명 (SIMPLE 타입에서 필수)", example = "감기약을 처방받고 싶습니다.")
        String description,

        // 상세 신청용 필드
        @Schema(description = "의약품명 (DETAILED 타입에서 필수)", example = "항생제 Amoxicillin")
        String medicineName,

        @Schema(description = "의약품 무게 (DETAILED 타입에서 필수)", example = "500mg")
        String medicineWeight,

        @Schema(description = "복용 횟수 (DETAILED 타입에서 필수)", example = "하루 3회")
        String dosageFrequency,

        @Schema(description = "복용 기간 (DETAILED 타입에서 필수)", example = "7일")
        String dosagePeriod,

        @Schema(description = "추가 요청사항 (선택사항)", example = "음식과 함께 복용하지 마세요.")
        String additionalNotes
) {
}
