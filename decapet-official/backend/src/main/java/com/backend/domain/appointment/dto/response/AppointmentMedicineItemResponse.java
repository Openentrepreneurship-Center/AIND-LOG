package com.backend.domain.appointment.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.backend.domain.appointment.entity.QuestionnaireAnswer;
import com.backend.domain.medicinecart.entity.MedicineItemType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 의약품 항목 응답")
public record AppointmentMedicineItemResponse(
        @Schema(description = "아이템 ID", example = "01JABCDEF123456789")
        String id,

        @Schema(description = "아이템 타입 (MEDICINE, PRESCRIPTION)", example = "MEDICINE")
        MedicineItemType itemType,

        @Schema(description = "의약품 ID", example = "med123")
        String medicineId,

        @Schema(description = "처방전 ID", example = "presc123")
        String prescriptionId,

        @Schema(description = "의약품 이름", example = "강아지 영양제")
        String medicineName,

        @Schema(description = "개당 가격", example = "15000")
        BigDecimal unitPrice,

        @Schema(description = "수량", example = "2")
        Integer quantity,

        @Schema(description = "소계 (단가 * 수량)", example = "30000")
        BigDecimal subtotal,

        @Schema(description = "의약품 이미지 URL")
        String imageUrl,

        @Schema(description = "설문 응답 목록")
        List<QuestionnaireAnswer> questionnaireAnswers
) {
}
