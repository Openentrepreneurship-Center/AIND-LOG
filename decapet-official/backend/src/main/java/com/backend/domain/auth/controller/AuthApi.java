package com.backend.domain.auth.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.auth.dto.request.LoginRequest;
import com.backend.domain.auth.dto.request.PasswordResetRequest;
import com.backend.domain.auth.dto.request.PasswordSmsSendRequest;
import com.backend.domain.auth.dto.request.RegisterRequest;
import com.backend.domain.auth.dto.request.SmsSendRequest;
import com.backend.domain.auth.dto.request.SmsVerifyRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "인증", description = "로그인, 회원가입, SMS 인증")
public interface AuthApi {

    @Operation(
            summary = "SMS 인증 코드 발송",
            description = """
                    휴대폰 번호로 6자리 인증 코드를 발송합니다.

                    **인증 규칙**
                    - 인증 코드 유효 시간: 5분
                    - 동일 번호로 재발송 시 기존 코드는 무효화
                    - 10분당 최대 5회 발송 가능 (Rate Limit)

                    **사용 화면**: 회원가입 > SMS 인증 단계
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 발송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "httpStatus": "OK",
                                      "message": "인증 코드가 발송되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 전화번호 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "G001",
                                      "errorMessage": "요청 값이 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "SMS 발송 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A010",
                                      "errorMessage": "SMS 발송에 실패했습니다.",
                                      "httpStatus": "INTERNAL_SERVER_ERROR"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> sendSms(@Valid SmsSendRequest request);

    @Operation(
            summary = "SMS 인증 코드 확인",
            description = """
                    발송된 인증 코드를 확인하고 회원가입용 토큰을 발급합니다.

                    **처리 흐름**
                    1. 인증 코드 검증 (6자리)
                    2. 성공 시 verificationToken 발급 (유효시간: 5분)
                    3. 이 토큰으로 회원가입 API 호출

                    **주의사항**
                    - 인증 코드 3회 실패 시 재발송 필요
                    - 토큰 만료 시 SMS 인증부터 재시작

                    **사용 화면**: 회원가입 > SMS 인증 완료 시 자동 호출
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공, 회원가입 토큰 발급",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "httpStatus": "OK",
                                      "message": "인증이 완료되었습니다.",
                                      "data": {
                                        "token": "550e8400-e29b-41d4-a716-446655440000"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증 코드 불일치 또는 만료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A005",
                                      "errorMessage": "인증 코드가 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> verifySms(@Valid SmsVerifyRequest request);

    @Operation(
            summary = "이메일 중복 확인",
            description = """
                    회원가입 전 이메일 사용 가능 여부를 확인합니다.

                    **응답 데이터**
                    - available: true → 사용 가능한 이메일
                    - available: false → 이미 등록된 이메일

                    **사용 화면**: 회원가입 > 이메일 입력 > 중복확인 버튼 클릭
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "사용 가능",
                                            value = """
                                                    {
                                                      "code": "A007",
                                                      "httpStatus": "OK",
                                                      "message": "이메일 중복 확인 완료",
                                                      "data": {
                                                        "available": true
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 등록됨",
                                            value = """
                                                    {
                                                      "code": "A007",
                                                      "httpStatus": "OK",
                                                      "message": "이메일 중복 확인 완료",
                                                      "data": {
                                                        "available": false
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 이메일 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "G001",
                                      "errorMessage": "요청 값이 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> checkEmailAvailable(
            @Parameter(description = "확인할 이메일 주소", example = "user@example.com", required = true)
            @Email(message = "이메일 형식이 올바르지 않습니다.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
            String email
    );

    @Operation(
            summary = "회원가입",
            description = """
                    SMS 인증 토큰으로 회원가입을 완료합니다.

                    **필수 조건**
                    - verificationToken: SMS 인증 완료 후 발급받은 토큰
                    - 필수 약관(SERVICE, PRIVACY) 동의 필요

                    **처리 흐름**
                    1. verificationToken 검증
                    2. 이메일/전화번호 중복 확인
                    3. 필수 약관 동의 확인
                    4. 회원 생성 + 약관 동의 기록
                    5. Access/Refresh Token을 HttpOnly 쿠키로 발급

                    **발급되는 권한**
                    - PRODUCT_PURCHASE: 상품 구매
                    - APPOINTMENT_BOOKING: 진료 예약
                    - INFORMATION_SHARING: 게시판 이용

                    **사용 화면**: 회원가입 > 정보 입력 > 가입 완료
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공 (Set-Cookie 헤더로 토큰 발급)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A003",
                                      "httpStatus": "CREATED",
                                      "message": "회원가입이 완료되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패 또는 필수 약관 미동의",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T005",
                                      "errorMessage": "필수 약관에 동의해주세요.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 또는 전화번호 중복",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U002",
                                      "errorMessage": "이미 등록된 이메일입니다.",
                                      "httpStatus": "CONFLICT"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "고유번호 생성 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U011",
                                      "errorMessage": "고유번호 생성에 실패했습니다.",
                                      "httpStatus": "INTERNAL_SERVER_ERROR"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> register(@Valid RegisterRequest request);

    @Operation(
            summary = "로그인",
            description = """
                    이메일/비밀번호로 로그인합니다.

                    **인증 방식**
                    - Access Token (30분): API 호출 시 사용
                    - Refresh Token (30일): Access Token 갱신용
                    - 모든 토큰은 HttpOnly 쿠키로 자동 전송

                    **처리 흐름**
                    1. 이메일로 사용자 조회
                    2. 비밀번호 검증
                    3. 약관 재동의 필요 여부 확인
                    4. Access/Refresh Token 발급 (Set-Cookie)

                    **로그인 후 상태**
                    - 쿠키에 토큰 저장됨
                    - 이후 API 호출 시 자동으로 인증 처리

                    **사용 화면**: 로그인 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 (Set-Cookie 헤더로 토큰 발급)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A004",
                                      "httpStatus": "OK",
                                      "message": "로그인에 성공했습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 비밀번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A009",
                                      "errorMessage": "비밀번호가 올바르지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "errorMessage": "사용자를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> login(@Valid LoginRequest request);

    @Operation(
            summary = "토큰 갱신",
            description = """
                    Refresh Token으로 새로운 Access/Refresh Token을 발급받습니다.

                    **Token Rotation 정책**
                    - 사용된 Refresh Token은 즉시 무효화
                    - 새로운 Refresh Token 발급
                    - 탈취된 토큰 재사용 시 전체 세션 무효화

                    **처리 흐름**
                    1. Refresh Token 검증 (쿠키에서 자동 추출)
                    2. 토큰 재사용 여부 확인 (보안 체크)
                    3. 기존 토큰 무효화
                    4. 새로운 Access/Refresh Token 발급

                    **호출 시점**
                    - Access Token 만료 시 (401 응답 수신 시)
                    - 프론트엔드에서 자동 갱신 처리 권장

                    **사용 화면**: 프론트엔드 인터셉터에서 자동 호출
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공 (Set-Cookie 헤더로 새 토큰 발급)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A005",
                                      "httpStatus": "OK",
                                      "message": "토큰이 갱신되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 Refresh Token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A011",
                                      "errorMessage": "보안 알림: 다시 로그인해주세요.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> refresh(@Parameter(hidden = true) HttpServletRequest request);

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 세션을 종료합니다.

                    **처리 내용**
                    - Refresh Token DB에서 삭제 (재사용 불가)
                    - Access/Refresh Token 쿠키 삭제

                    **로그아웃 후 상태**
                    - 모든 인증 필요 API 호출 시 401 반환
                    - 다시 로그인 필요

                    **사용 화면**: 마이페이지 > 로그아웃 버튼
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공 (쿠키 삭제됨)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A006",
                                      "httpStatus": "OK",
                                      "message": "로그아웃되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> logout(@Parameter(hidden = true) HttpServletRequest request);

    @Operation(
            summary = "비밀번호 찾기 SMS 발송",
            description = """
                    비밀번호 재설정을 위한 SMS 인증 코드를 발송합니다.

                    **인증 규칙**
                    - 인증 코드 유효 시간: 5분
                    - 동일 번호로 재발송 시 기존 코드는 무효화
                    - 10분당 최대 5회 발송 가능 (Rate Limit)

                    **주의사항**
                    - 이메일과 전화번호가 모두 일치해야 발송 가능
                    - 계정 정보가 일치하지 않으면 404 에러

                    **사용 화면**: 로그인 > 비밀번호 찾기 > SMS 인증 단계
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 발송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A008",
                                      "httpStatus": "OK",
                                      "message": "비밀번호 재설정 인증 코드가 발송되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 전화번호 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "G001",
                                      "errorMessage": "요청 값이 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계정 또는 전화번호 정보 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A016",
                                      "errorMessage": "계정 또는 전화번호 정보가 올바르지 않습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> sendPasswordResetSms(@Valid PasswordSmsSendRequest request);

    @Operation(
            summary = "비밀번호 찾기 SMS 검증",
            description = """
                    발송된 인증 코드를 확인하고 비밀번호 재설정용 토큰을 발급합니다.

                    **처리 흐름**
                    1. 인증 코드 검증 (6자리)
                    2. 성공 시 verificationToken 발급 (유효시간: 5분)
                    3. 이 토큰으로 비밀번호 재설정 API 호출

                    **주의사항**
                    - 인증 코드 3회 실패 시 재발송 필요
                    - 토큰 만료 시 SMS 인증부터 재시작

                    **사용 화면**: 비밀번호 찾기 > SMS 인증 완료 시 자동 호출
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공, 비밀번호 재설정 토큰 발급",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A009",
                                      "httpStatus": "OK",
                                      "message": "인증이 완료되었습니다.",
                                      "data": {
                                        "token": "550e8400-e29b-41d4-a716-446655440000"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증 코드 불일치 또는 만료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A005",
                                      "errorMessage": "인증 코드가 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> verifyPasswordResetSms(@Valid SmsVerifyRequest request);

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    SMS 인증 토큰으로 비밀번호를 재설정합니다.

                    **필수 조건**
                    - token: SMS 인증 완료 후 발급받은 토큰
                    - phone: 인증한 전화번호
                    - newPassword: 새 비밀번호 (8자 이상, 영문/숫자/특수문자 포함)

                    **처리 흐름**
                    1. 토큰 검증
                    2. 전화번호로 사용자 조회
                    3. 비밀번호 변경
                    4. 토큰 사용 처리
                    5. 모든 기존 세션 무효화 (보안)

                    **보안 처리**
                    - 비밀번호 변경 시 기존 모든 세션(Access/Refresh Token) 무효화
                    - 사용된 토큰은 재사용 불가

                    **사용 화면**: 비밀번호 찾기 > 새 비밀번호 입력 > 변경 완료
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A010",
                                      "httpStatus": "OK",
                                      "message": "비밀번호가 변경되었습니다.",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패 또는 토큰 이미 사용됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A007",
                                      "errorMessage": "이미 사용된 토큰입니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "errorMessage": "사용자를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> resetPassword(@Valid PasswordResetRequest request);
}
