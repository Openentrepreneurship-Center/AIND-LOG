package com.backend.domain.terms.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 동의 요청")
public record TermConsentRequest(
        @Schema(description = "약관 동의 정보 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "약관 동의 정보를 입력해주세요.")
        @Valid
        List<TermConsent> consents
) {
    @Schema(description = "개별 약관 동의 정보")
    public record TermConsent(
            @Schema(description = "약관 ID", example = "term123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "약관 ID를 입력해주세요.")
            String termId,

            @Schema(description = "동의 여부 (필수 약관은 true여야 함)", example = "true")
            boolean agreed
    ) {
    }
}
