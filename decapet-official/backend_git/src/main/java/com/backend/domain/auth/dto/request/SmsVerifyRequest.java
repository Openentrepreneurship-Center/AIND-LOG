package com.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "SMS 인증 코드 확인 요청")
public record SmsVerifyRequest(
        @Schema(description = "휴대폰 번호 (하이픈 없이)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phone,

        @Schema(description = "6자리 인증 코드", example = "123456")
        @NotBlank(message = "인증 코드를 입력해주세요.")
        @Size(min = 6, max = 6, message = "인증 코드는 6자리입니다.")
        String code
) {
}
