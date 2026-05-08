package com.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "연락처 변경 SMS 발송 요청")
public record PhoneChangeRequest(
        @Schema(description = "새 전화번호 (010으로 시작하는 11자리)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phone
) {
}
