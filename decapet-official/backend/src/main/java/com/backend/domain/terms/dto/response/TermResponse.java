package com.backend.domain.terms.dto.response;

import java.time.LocalDate;

import com.backend.domain.terms.entity.TermType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 정보 응답")
public record TermResponse(
        @Schema(description = "약관 ID", example = "term123")
        String id,

        @Schema(description = "약관 유형 (SERVICE: 서비스 이용약관, PRIVACY: 개인정보 처리방침)", example = "SERVICE")
        TermType type,

        @Schema(description = "약관 버전", example = "1")
        int version,

        @Schema(description = "약관 내용", example = "서비스 이용약관 내용...")
        String content,

        @Schema(description = "필수 약관 여부", example = "true")
        boolean isRequired,

        @Schema(description = "약관 시행일", example = "2025-01-01")
        LocalDate effectiveDate
) {
}
