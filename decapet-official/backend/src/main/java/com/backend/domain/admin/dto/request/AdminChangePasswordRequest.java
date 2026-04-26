package com.backend.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 비밀번호 변경 요청")
public record AdminChangePasswordRequest(
        @Schema(description = "현재 비밀번호", example = "OldPass123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

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
