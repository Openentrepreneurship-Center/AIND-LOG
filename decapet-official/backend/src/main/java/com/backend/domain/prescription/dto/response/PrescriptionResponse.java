package com.backend.domain.prescription.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.pet.entity.Gender;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.entity.PrescriptionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "처방전 응답")
public record PrescriptionResponse(
        @Schema(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "사용자 ID", example = "user123")
        String userId,

        @Schema(description = "사용자 이름", example = "홍길동")
        String userName,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String userEmail,

        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        String userPhone,

        @Schema(description = "반려동물 ID", example = "pet123")
        String petId,

        @Schema(description = "반려동물 이름", example = "멍멍이")
        String petName,

        @Schema(description = "반려동물 성별", example = "MALE")
        Gender petGender,

        @Schema(description = "반려동물 생년월일", example = "2020-01-15")
        LocalDate petBirthdate,

        @Schema(description = "반려동물 몸무게(kg)", example = "5.5")
        BigDecimal petWeight,

        @Schema(description = "신청 유형 (SIMPLE: 간단 신청, DETAILED: 상세 신청)", example = "DETAILED")
        PrescriptionType type,

        @Schema(description = "간단 설명 (SIMPLE 타입)", example = "감기약을 처방받고 싶습니다.")
        String description,

        @Schema(description = "첨부파일 URL 목록 (DETAILED 타입)", example = "[\"https://s3.amazonaws.com/bucket/prescription1.jpg\"]")
        List<String> attachmentUrls,

        @Schema(description = "의약품명 (DETAILED 타입)", example = "항생제 Amoxicillin")
        String medicineName,

        @Schema(description = "의약품 무게 (DETAILED 타입)", example = "500mg")
        String medicineWeight,

        @Schema(description = "복용 횟수 (DETAILED 타입)", example = "하루 3회")
        String dosageFrequency,

        @Schema(description = "복용 기간 (DETAILED 타입)", example = "7일")
        String dosagePeriod,

        @Schema(description = "추가 요청사항", example = "음식과 함께 복용하지 마세요.")
        String additionalNotes,

        @Schema(description = "처방전 상태 (PENDING, APPROVED, REJECTED)", example = "PENDING")
        PrescriptionStatus status,

        @Schema(description = "결제 금액 (관리자 승인 시 설정)", example = "50000")
        BigDecimal price,

        @Schema(description = "의약품 이미지 URL (관리자 승인 시 설정)", example = "https://s3.amazonaws.com/bucket/prescription-image.jpg")
        String imageUrl,

        @Schema(description = "처방전 생성 일시", example = "2025-12-16T10:30:00")
        LocalDateTime createdAt
) {
}
