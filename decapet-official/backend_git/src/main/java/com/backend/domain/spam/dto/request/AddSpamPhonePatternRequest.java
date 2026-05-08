package com.backend.domain.spam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSpamPhonePatternRequest(
    @NotBlank(message = "패턴은 필수입니다.")
    @Size(max = 20, message = "패턴은 최대 20자까지 입력 가능합니다.")
    String pattern
) {
}
