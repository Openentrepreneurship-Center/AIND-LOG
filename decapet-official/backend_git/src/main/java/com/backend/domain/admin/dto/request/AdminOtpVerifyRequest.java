package com.backend.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "관리자 OTP 검증 요청 (2단계: OTP 인증)")
public record AdminOtpVerifyRequest(
        @Schema(description = "OTP 코드 (6자리)", example = "123456")
        @NotBlank(message = "OTP 코드를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "OTP 코드는 6자리 숫자입니다.")
        String otpCode
) {
}
