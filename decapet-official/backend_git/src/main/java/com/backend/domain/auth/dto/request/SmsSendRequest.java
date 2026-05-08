package com.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "SMS 인증 코드 발송 요청")
public record SmsSendRequest(
        @Schema(description = "휴대폰 번호 (하이픈 없이)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phone
) {
}
