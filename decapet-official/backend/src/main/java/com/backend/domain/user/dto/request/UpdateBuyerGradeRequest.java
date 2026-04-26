package com.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 구매자 등급 및 메모 수정 요청")
public record UpdateBuyerGradeRequest(
        @Schema(description = "구매자 등급 (1, 2, 3, 4 또는 null)", example = "1")
        @Pattern(regexp = "^[1-4]$", message = "구매자 등급은 1, 2, 3, 4 중 하나여야 합니다.")
        String buyerGrade,

        @Schema(description = "관리자 메모 (최대 1000자)", example = "메모")
        @Size(max = 1000, message = "관리자 메모는 1000자 이하여야 합니다.")
        String adminMemo,

        @Schema(description = "관리자 메모 2 (최대 1000자)", example = "추가 메모")
        @Size(max = 1000, message = "관리자 메모 2는 1000자 이하여야 합니다.")
        String adminMemo2
) {
}
