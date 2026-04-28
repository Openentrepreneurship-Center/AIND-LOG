package com.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.backend.global.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "연락처 변경 SMS 인증 요청")
public record PhoneVerifyRequest(
        @Schema(description = "새 전화번호 (010으로 시작하는 11자리)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        String phone,

        @Schema(description = "인증번호 (6자리)", example = "123456")
        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {
}
