package com.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMS 인증 완료 응답")
public record VerificationTokenResponse(
        @Schema(description = "회원가입용 인증 토큰 (5분간 유효)", example = "550e8400-e29b-41d4-a716-446655440000")
        String token
) {
}
