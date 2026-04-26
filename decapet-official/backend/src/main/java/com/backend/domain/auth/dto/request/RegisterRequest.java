package com.backend.domain.auth.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record RegisterRequest(
        @Schema(description = "SMS 인증 후 발급받은 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "인증 토큰을 입력해주세요.")
        String token,

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        String email,

        @Schema(description = "비밀번호 (영문+숫자+특수문자 8자 이상)", example = "Password123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
        String password,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        String name,

        @Schema(description = "휴대폰 번호 (하이픈 없이)", example = "01012345678")
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phone,

        @Schema(description = "우편번호 (5자리)", example = "12345")
        @NotBlank(message = "우편번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipCode,

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        @NotBlank(message = "주소를 입력해주세요.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @Schema(description = "상세주소", example = "101동 1001호")
        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
        String detailAddress,

        @Schema(description = "약관 동의 목록")
        @NotEmpty(message = "약관 동의 정보를 입력해주세요.")
        @Valid
        List<TermConsentInfo> termConsents
) {
}
