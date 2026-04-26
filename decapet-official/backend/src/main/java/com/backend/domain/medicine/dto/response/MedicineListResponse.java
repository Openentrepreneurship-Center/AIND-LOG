package com.backend.domain.medicine.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의약품 목록 응답 (페이지네이션 포함)")
public record MedicineListResponse(
        @Schema(description = "의약품 목록")
        List<MedicineResponse> medicines,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 의약품 수", example = "25")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "2")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
}
