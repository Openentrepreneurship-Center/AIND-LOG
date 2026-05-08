package com.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 중복 확인 응답")
public record EmailCheckResponse(

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available
) {
}
