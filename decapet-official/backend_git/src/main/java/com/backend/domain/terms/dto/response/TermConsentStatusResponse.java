package com.backend.domain.terms.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 동의 상태 응답")
public record TermConsentStatusResponse(
        @Schema(description = "재동의 필요 여부 (true인 경우 사용자에게 재동의 요청 필요)", example = "true")
        boolean reconsentRequired,

        @Schema(description = "재동의가 필요한 약관 목록 (업데이트된 약관)")
        List<TermResponse> pendingTerms
) {
}
