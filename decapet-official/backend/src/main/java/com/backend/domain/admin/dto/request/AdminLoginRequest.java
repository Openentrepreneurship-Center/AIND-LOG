package com.backend.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 로그인 요청 (1단계: ID/PW 인증)")
public record AdminLoginRequest(
        @Schema(description = "관리자 아이디", example = "admin")
        @NotBlank(message = "아이디를 입력해주세요.")
        String loginId,

        @Schema(description = "비밀번호", example = "SecurePassword123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
