package com.backend.domain.banner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메인 배너 응답 (사용자용)")
public record MainBannerResponse(
        @Schema(description = "배너 ID", example = "banner123")
        String id,

        @Schema(description = "배너 제목", example = "신규 회원 가입 이벤트")
        String title,

        @Schema(description = "배너 이미지 URL", example = "https://example.com/banner1.jpg")
        String imageUrl,

        @Schema(description = "배너 표시 순서 (낮은 숫자가 먼저 표시됨)", example = "1")
        int displayOrder
) {
}
