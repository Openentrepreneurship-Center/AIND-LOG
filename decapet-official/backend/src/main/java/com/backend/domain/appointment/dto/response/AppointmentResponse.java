package com.backend.domain.appointment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.pet.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 응답")
public record AppointmentResponse(
        @Schema(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "타임슬롯 ID", example = "slot123")
        String timeSlotId,

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

        @Schema(description = "예약 날짜", example = "2025-12-20")
        LocalDate appointmentDate,

        @Schema(description = "예약 시간", example = "14:00:00")
        LocalTime appointmentTime,

        @Schema(description = "예약 장소", example = "서울시 강남구 테헤란로 123")
        String location,

        @Schema(description = "처방 의약품 목록")
        List<AppointmentMedicineItemResponse> medicines,

        @Schema(description = "총 결제 금액", example = "30000")
        BigDecimal totalAmount,

        @Schema(description = "예약 상태 (PENDING, APPROVED, REJECTED, COMPLETED)", example = "PENDING")
        AppointmentStatus status,

        @Schema(description = "결제 상태", example = "COMPLETED")
        String paymentStatus,

        @Schema(description = "결제 수단", example = "BANK_TRANSFER")
        String paymentMethod,

        @Schema(description = "입금자명", example = "홍길동")
        String accountHolder,

        @Schema(description = "현금영수증 유형 (PERSONAL, BUSINESS, NONE)", example = "PERSONAL")
        String cashReceiptType,

        @Schema(description = "현금영수증 번호", example = "01012345678")
        String cashReceiptNumber,

        @Schema(description = "예약 생성 일시", example = "2025-12-16T10:30:00")
        LocalDateTime createdAt
) {
}
