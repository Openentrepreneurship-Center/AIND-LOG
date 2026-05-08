package com.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest(
        @Schema(description = "SMS 인증 후 발급받은 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "토큰을 입력해주세요.")
        String token,

        @Schema(description = "휴대폰 번호 (하이픈 없이)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phone,

        @Schema(description = "새 비밀번호 (8자 이상, 영문/숫자/특수문자 포함)", example = "NewPass123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        String newPassword
) {
}
