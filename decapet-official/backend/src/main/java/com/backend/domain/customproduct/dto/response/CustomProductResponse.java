package com.backend.domain.customproduct.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.customproduct.entity.CustomProductStatus;
import com.backend.domain.pet.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커스텀 상품 응답")
public record CustomProductResponse(
        @Schema(description = "커스텀 상품 ID", example = "67890abcdef12345")
        String id,

        @Schema(description = "신청자 이름", example = "홍길동")
        String userName,

        @Schema(description = "신청자 이메일", example = "hong@email.com")
        String userEmail,

        @Schema(description = "신청자 전화번호", example = "01012345678")
        String userPhone,

        @Schema(description = "반려동물 ID", example = "01HXYZ...")
        String petId,

        @Schema(description = "반려동물 이름", example = "멍멍이")
        String petName,

        @Schema(description = "반려동물 성별", example = "MALE", nullable = true)
        Gender petGender,

        @Schema(description = "반려동물 생년월일", example = "2023-03-15", nullable = true)
        LocalDate petBirthdate,

        @Schema(description = "반려동물 몸무게(kg)", example = "5.2", nullable = true)
        BigDecimal petWeight,

        @Schema(description = "상품명", example = "강아지 맞춤 사료")
        String name,

        @Schema(description = "상품 설명", example = "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다.")
        String description,

        @Schema(description = "신청자가 요청한 희망 가격", example = "50000")
        BigDecimal requestedPrice,

        @Schema(description = "관리자가 승인한 최종 가격", example = "55000", nullable = true)
        BigDecimal approvedPrice,

        @Schema(description = "정가", example = "60000", nullable = true)
        BigDecimal basePrice,

        @Schema(description = "상품 무게(kg)", example = "2.5", nullable = true)
        BigDecimal weight,

        @Schema(description = "상품 개수 (1세트당 개수)", example = "3")
        Integer quantity,

        @Schema(description = "상품 이미지 URL", example = "https://s3.amazonaws.com/decapet/custom-products/12345.jpg")
        String imageUrl,

        @Schema(description = "신청 상태 (PENDING: 대기, APPROVED: 승인, REJECTED: 거절)", example = "APPROVED")
        CustomProductStatus status,

        @Schema(description = "수량 추가 가능 여부 (장바구니에서 수량 증가 허용)", example = "true", nullable = true)
        Boolean allowMultiple,

        @Schema(description = "재고 수량", example = "10")
        int stockQuantity,

        @Schema(description = "기타 상품 정보", example = "[\"원산지: 한국\", \"성분: 유기농\"]", nullable = true)
        List<String> additionalInfo,

        @Schema(description = "유효기간 (승인일 + 1년)", example = "2026-12-16T10:00:00", nullable = true)
        LocalDateTime expirationDate,

        @Schema(description = "신청 일시", example = "2025-12-16T10:00:00")
        LocalDateTime createdAt
) {
}
