package com.backend.domain.product.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 목록 응답 (페이지네이션 포함)")
public record ProductListResponse(
        @Schema(description = "상품 목록")
        List<ProductSummaryResponse> products,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 상품 수", example = "45")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
}
