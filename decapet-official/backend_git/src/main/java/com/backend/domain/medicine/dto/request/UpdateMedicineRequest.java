package com.backend.domain.medicine.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.backend.domain.medicine.dto.internal.QuestionnaireItem;
import com.backend.domain.medicine.entity.MedicineSalesStatus;
import com.backend.domain.medicine.entity.MedicineType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "의약품 수정 요청", example = """
        {
          "name": "넥스가드 스펙트라 (수정)",
          "description": "업데이트된 설명입니다.",
          "price": 38000,
          "type": "PARASITE",
          "minWeight": 2.5,
          "maxWeight": 65.0,
          "expirationDate": "2026-12-31",
          "monthsPerUnit": 3,
          "questionnaire": [
            {
              "questionId": "q1",
              "question": "수정된 질문입니다."
            }
          ]
        }""")
public record UpdateMedicineRequest(
        @Schema(description = "의약품명 (필수)", example = "넥스가드 스펙트라", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "의약품명을 입력해주세요.")
        String name,

        @Schema(description = "의약품 설명 (선택)", example = "개에 대한 벼룩, 진드기 및 내부 기생충 예방 및 치료")
        String description,

        @Schema(description = "가격 (필수)", example = "35000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "가격을 입력해주세요.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        BigDecimal price,

        @Schema(description = "의약품 타입 (필수) - PARASITE: 기생충예방, SKIN: 피부, COGNITIVE: 인지기능, OTHER: 기타", example = "PARASITE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "의약품 타입을 선택해주세요.")
        MedicineType type,

        @Schema(description = "최소 적용 체중 (kg, 선택)", example = "2.0")
        BigDecimal minWeight,

        @Schema(description = "최대 적용 체중 (kg, 선택)", example = "60.0")
        BigDecimal maxWeight,

        @Schema(description = "유통기한 (필수)", example = "2025-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "유통기한을 입력해주세요.")
        LocalDate expirationDate,

        @Schema(description = "1개당 처방 개월 수 (기생충약 타입일 때 필수, 예: 3이면 1개=3개월)", example = "3")
        @Positive(message = "처방 개월 수는 0보다 커야 합니다.")
        Integer monthsPerUnit,

        @Schema(description = "처방 설문지 목록 (필수)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "문진표를 입력해주세요.")
        @NotEmpty(message = "문진표는 최소 1개 이상 입력해주세요.")
        List<QuestionnaireItem> questionnaire,

        @Schema(description = "판매가 (선택, 미입력 시 정가와 동일)", example = "30000")
        @Positive(message = "판매가는 0보다 커야 합니다.")
        BigDecimal salePrice,

        @Schema(description = "판매 상태 (선택) - ON_SALE: 판매중, SUSPENDED: 판매 중단", example = "ON_SALE")
        MedicineSalesStatus salesStatus,

        @Schema(description = "의약품 추가 정보 (선택)", example = "[\"제조사: Alpha Labs\", \"원산지: NZ\"]")
        List<String> additionalInfo
) {
}
