package com.backend.domain.terms.dto.request;

import java.time.LocalDate;

import com.backend.domain.terms.entity.TermType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 생성 요청")
public record CreateTermRequest(
        @Schema(description = "약관 유형 (SERVICE: 서비스 이용약관, PRIVACY: 개인정보 처리방침)", example = "SERVICE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "약관 유형을 선택해주세요.")
        TermType type,

        @Schema(description = "약관 내용", example = "서비스 이용약관 내용...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용을 입력해주세요.")
        String content,

        @Schema(description = "필수 약관 여부", example = "true")
        boolean isRequired,

        @Schema(description = "약관 시행일 (기존 최신 버전의 시행일 이후여야 함)", example = "2025-06-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "시행일을 입력해주세요.")
        LocalDate effectiveDate
) {
}
