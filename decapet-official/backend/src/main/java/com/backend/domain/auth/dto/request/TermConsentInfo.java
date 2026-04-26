package com.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "약관 동의 정보")
public record TermConsentInfo(
        @Schema(description = "약관 ID", example = "01HXK5...")
        @NotBlank(message = "약관 ID를 입력해주세요.")
        String termId
) {
}
