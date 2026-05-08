package com.backend.domain.banner.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 정보 응답")
public record BannerResponse(
        @Schema(description = "배너 ID", example = "banner123")
        String id,

        @Schema(description = "배너 제목", example = "신규 회원 가입 이벤트")
        String title,

        @Schema(description = "배너 이미지 URL", example = "https://example.com/banner1.jpg")
        String imageUrl,

        @Schema(description = "배너 표시 순서 (낮은 숫자가 먼저 표시됨)", example = "1")
        int displayOrder,

        @Schema(description = "배너 시작 일시", example = "2025-12-01T00:00:00")
        LocalDateTime startAt,

        @Schema(description = "배너 종료 일시", example = "2025-12-31T23:59:59")
        LocalDateTime endAt
) {
}
