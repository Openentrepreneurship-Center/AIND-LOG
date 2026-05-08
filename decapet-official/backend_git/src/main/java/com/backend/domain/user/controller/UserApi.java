package com.backend.domain.user.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.user.dto.request.ChangePasswordRequest;
import com.backend.domain.user.dto.request.PhoneChangeRequest;
import com.backend.domain.user.dto.request.PhoneVerifyRequest;
import com.backend.domain.user.dto.request.UpdateProfileRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "사용자", description = "프로필 관리")
public interface UserApi {

    @Operation(
            summary = "내 정보 조회",
            description = """
                    로그인한 사용자의 정보를 조회합니다.

                    **응답 정보**
                    - 사용자 ID (MongoDB ObjectId)
                    - 이메일
                    - 전화번호
                    - 이름
                    - 우편번호
                    - 주소

                    **사용 화면**: 마이페이지 > 내 정보
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "httpStatus": "OK",
                                      "message": "회원 정보 조회 성공",
                                      "data": {
                                        "id": "507f1f77bcf86cd799439011",
                                        "email": "hong@example.com",
                                        "phone": "01012345678",
                                        "name": "홍길동",
                                        "zipCode": "06234",
                                        "address": "서울특별시 강남구 테헤란로 123",
                                        "recipientName": "홍길동",
                                        "recipientPhone": "01012345678",
                                        "permissions": ["PRODUCT_PURCHASE", "APPOINTMENT_BOOKING"]
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
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
    ResponseEntity<SuccessResponse> getMe(@Parameter(hidden = true) String userId);

    @Operation(
            summary = "회원 정보 수정",
            description = """
                    로그인한 사용자의 프로필 정보를 수정합니다.

                    **수정 가능 항목**
                    - 이름: 2~50자
                    - 전화번호: 010으로 시작하는 11자리 (예: 01012345678)
                    - 우편번호: 5자리 숫자
                    - 주소: 최대 255자

                    **수정 불가 항목**
                    - 이메일 (고유 식별자로 사용)

                    **처리 방식**
                    - Partial Update: 제공된 필드만 수정
                    - null 또는 누락된 필드는 기존 값 유지

                    **사용 화면**: 마이페이지 > 정보 수정
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U002",
                                      "httpStatus": "OK",
                                      "message": "회원 정보 수정 성공",
                                      "data": {
                                        "id": "507f1f77bcf86cd799439011",
                                        "email": "hong@example.com",
                                        "phone": "01087654321",
                                        "name": "홍길동",
                                        "zipCode": "06234",
                                        "address": "서울특별시 강남구 테헤란로 456",
                                        "recipientName": "김수령",
                                        "recipientPhone": "01098765432",
                                        "permissions": ["PRODUCT_PURCHASE", "APPOINTMENT_BOOKING"]
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패",
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
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "전화번호 중복",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U003",
                                      "errorMessage": "이미 등록된 전화번호입니다.",
                                      "httpStatus": "CONFLICT"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> updateProfile(
            @Parameter(hidden = true) String userId,
            @Valid UpdateProfileRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    계정을 탈퇴합니다.

                    **처리 방식**
                    - Soft Delete: 데이터는 30일간 보관 후 완전 삭제
                    - 탈퇴 즉시 모든 토큰 무효화
                    - 쿠키 삭제 처리

                    **탈퇴 제한 조건**
                    - 배송 중인 주문이 있는 경우
                    - 미결제 주문이 있는 경우
                    - 진행 중인 진료 예약이 있는 경우

                    **탈퇴 후 상태**
                    - 로그인 불가
                    - 동일 이메일로 30일간 재가입 불가
                    - 반려동물/주문 이력 등 모든 데이터 삭제

                    **사용 화면**: 마이페이지 > 설정 > 회원 탈퇴
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U003",
                                      "httpStatus": "OK",
                                      "message": "회원 탈퇴 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
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
    ResponseEntity<SuccessResponse> deleteAccount(@Parameter(hidden = true) String userId);

    @Operation(
            summary = "연락처 변경 SMS 발송",
            description = """
                    새 전화번호로 인증 코드를 발송합니다.

                    **처리 방식**
                    - 새 전화번호가 다른 사용자와 중복되는지 확인
                    - 인증 코드 발송 (5분 유효)

                    **사용 화면**: 마이페이지 > 회원 정보 관리 > 연락처 수정
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            @ApiResponse(responseCode = "409", description = "전화번호 중복")
    })
    ResponseEntity<SuccessResponse> sendPhoneChangeSms(
            @Parameter(hidden = true) String userId,
            @Valid PhoneChangeRequest request
    );

    @Operation(
            summary = "연락처 변경 SMS 인증",
            description = """
                    인증 코드를 확인하고 연락처를 변경합니다.

                    **처리 방식**
                    - 인증 코드 검증
                    - 성공 시 연락처 즉시 변경

                    **사용 화면**: 마이페이지 > 회원 정보 관리 > 연락처 수정
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연락처 변경 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    })
    ResponseEntity<SuccessResponse> verifyPhoneChange(
            @Parameter(hidden = true) String userId,
            @Valid PhoneVerifyRequest request
    );

    @Operation(
            summary = "비밀번호 변경",
            description = """
                    현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.

                    **처리 방식**
                    - 현재 비밀번호 일치 여부 확인
                    - 새 비밀번호로 변경
                    - 기존 모든 세션 무효화 (재로그인 필요)

                    **비밀번호 규칙**
                    - 8자 이상
                    - 영문, 숫자, 특수문자(@$!%*#?&) 포함

                    **사용 화면**: 마이페이지 > 회원 정보 관리 > 비밀번호 변경
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치")
    })
    ResponseEntity<SuccessResponse> changePassword(
            @Parameter(hidden = true) String userId,
            @Parameter(hidden = true) jakarta.servlet.http.HttpServletRequest httpRequest,
            @Valid ChangePasswordRequest request
    );
}
