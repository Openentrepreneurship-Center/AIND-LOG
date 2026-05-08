package com.backend.domain.admin.dto.response;

import com.backend.domain.admin.entity.AdminLoginStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 로그인 응답")
public record AdminLoginResponse(
        @Schema(description = "로그인 상태", example = "OTP_REQUIRED")
        AdminLoginStatus status
) {
}
