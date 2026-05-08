package com.backend.domain.banner.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "배너 생성 요청", example = """
        {
          "title": "신규 회원 가입 이벤트",
          "startAt": "2025-12-01T00:00:00",
          "endAt": "2025-12-31T23:59:59"
        }""")
public record CreateBannerRequest(
        @Schema(description = "배너 제목", example = "신규 회원 가입 이벤트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @Schema(description = "배너 시작 일시", example = "2025-12-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "시작 일시를 입력해주세요.")
        LocalDateTime startAt,

        @Schema(description = "배너 종료 일시", example = "2025-12-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "종료 일시를 입력해주세요.")
        LocalDateTime endAt
) {
}
