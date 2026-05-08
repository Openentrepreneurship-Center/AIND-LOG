package com.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 프로필 수정 요청")
public record UpdateProfileRequest(
        @Schema(description = "회원 이름 (2~50자)", example = "홍길동")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        String name,

        @Schema(description = "우편번호 (5자리 숫자)", example = "06234")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipCode,

        @Schema(description = "주소 (최대 255자)", example = "서울특별시 강남구 테헤란로 123")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @Schema(description = "상세주소 (최대 100자)", example = "101동 1001호")
        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
        String detailAddress,

        @Schema(description = "수령인 이름 (2~50자)", example = "홍길동")
        @Size(min = 2, max = 50, message = "수령인 이름은 2자 이상 50자 이하여야 합니다.")
        String recipientName,

        @Schema(description = "수령인 연락처 (010으로 시작하는 11자리)", example = "01012345678")
        @Pattern(regexp = "^010\\d{8}$", message = "수령인 연락처 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String recipientPhone
) {
}
