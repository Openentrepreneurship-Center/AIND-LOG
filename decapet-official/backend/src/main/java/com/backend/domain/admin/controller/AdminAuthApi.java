package com.backend.domain.admin.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.admin.dto.request.AdminChangePasswordRequest;
import com.backend.domain.admin.dto.request.AdminLoginRequest;
import com.backend.domain.admin.dto.request.AdminOtpVerifyRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "[Admin] 인증", description = "관리자 인증")
public interface AdminAuthApi {

	@Operation(
		summary = "관리자 로그인",
		description = """
			관리자 아이디와 비밀번호로 로그인합니다.

			**처리 흐름**
			1. 아이디로 관리자 계정 조회
			2. 비밀번호 검증
			3. OTP 설정 여부에 따라 OTP_REQUIRED 또는 OTP_SETUP_REQUIRED 반환

			**사용 화면**: 관리자 > 로그인
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "1단계 인증 성공 (OTP 필요)",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AD002",
					  "httpStatus": "OK",
					  "message": "관리자 로그인 성공",
					  "data": { "status": "OTP_REQUIRED" }
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "비밀번호 불일치",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AD004",
					  "errorMessage": "비밀번호가 올바르지 않습니다.",
					  "httpStatus": "UNAUTHORIZED"
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "관리자를 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AD001",
					  "errorMessage": "관리자를 찾을 수 없습니다.",
					  "httpStatus": "NOT_FOUND"
					}
					""")
			)
		)
	})
	ResponseEntity<SuccessResponse> login(@Valid AdminLoginRequest request);

	@Operation(summary = "OTP 설정", description = "관리자 계정에 OTP(2단계 인증)를 설정합니다.")
	ResponseEntity<SuccessResponse> setupOtp(@Parameter(hidden = true) HttpServletRequest request);

	@Operation(summary = "2단계: OTP 검증 및 로그인 완료", description = "OTP 코드를 검증하고 로그인을 완료합니다.")
	ResponseEntity<SuccessResponse> verifyOtp(
		@Parameter(hidden = true) HttpServletRequest request,
		@Valid AdminOtpVerifyRequest otpRequest
	);

	@Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급받습니다.")
	ResponseEntity<SuccessResponse> refresh(@Parameter(hidden = true) HttpServletRequest request);

	@Operation(summary = "로그아웃", description = "현재 관리자 세션을 종료합니다.")
	ResponseEntity<SuccessResponse> logout(@Parameter(hidden = true) HttpServletRequest request);

	@Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인 후 새 비밀번호로 변경합니다. 변경 후 모든 세션이 만료됩니다.")
	ResponseEntity<SuccessResponse> changePassword(
		@Parameter(hidden = true) HttpServletRequest request,
		@Parameter(hidden = true) String adminId,
		@Valid AdminChangePasswordRequest passwordRequest
	);
}
