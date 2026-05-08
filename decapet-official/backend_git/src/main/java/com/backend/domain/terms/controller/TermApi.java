package com.backend.domain.terms.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.terms.dto.request.TermConsentRequest;
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

@Tag(name = "약관", description = "약관 조회 및 동의")
public interface TermApi {

    @Operation(
            summary = "현재 약관 목록 조회",
            description = """
                    현재 유효한 약관 목록을 조회합니다.

                    **비즈니스 로직**
                    - 현재 시행 중인 최신 버전의 약관만 반환
                    - 서비스 이용약관과 개인정보 처리방침 포함
                    - 인증 없이 조회 가능한 공개 API

                    **응답 정보**
                    - 약관 ID, 타입, 버전
                    - 약관 내용
                    - 필수 여부, 시행일

                    **사용 화면**: 회원가입 페이지, 약관 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T001",
                                      "httpStatus": "OK",
                                      "message": "약관 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "term123",
                                          "type": "SERVICE",
                                          "version": 1,
                                          "content": "서비스 이용약관 내용...",
                                          "isRequired": true,
                                          "effectiveDate": "2025-01-01"
                                        },
                                        {
                                          "id": "term456",
                                          "type": "PRIVACY",
                                          "version": 1,
                                          "content": "개인정보 처리방침 내용...",
                                          "isRequired": true,
                                          "effectiveDate": "2025-01-01"
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getCurrentTerms();

    @Operation(
            summary = "약관 동의",
            description = """
                    약관에 동의합니다.

                    **비즈니스 로직**
                    - 필수 약관은 반드시 동의 필요
                    - 여러 약관을 한 번에 동의 처리 가능
                    - 이미 동의한 약관에 대해 재동의 가능

                    **제약사항**
                    - 필수 약관 미동의 시 오류 반환

                    **처리 흐름**
                    1. 약관 내용 확인
                    2. 동의 처리
                    3. 회원가입 또는 서비스 이용 진행

                    **사용 화면**: 회원가입 페이지, 약관 동의 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 동의 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T003",
                                      "httpStatus": "OK",
                                      "message": "약관 동의 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수 약관 미동의",
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
                    responseCode = "404",
                    description = "약관을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T001",
                                      "errorMessage": "약관을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> acceptTerms(
            @Parameter(hidden = true)
            String userId,
            @Valid TermConsentRequest request
    );

    @Operation(
            summary = "약관 동의 상태 조회",
            description = """
                    사용자의 약관 동의 상태를 조회합니다.

                    **비즈니스 로직**
                    - 재동의가 필요한 약관이 있는지 확인
                    - 약관이 업데이트되어 재동의가 필요한 경우 해당 약관 목록 반환
                    - reconsentRequired가 true인 경우 사용자에게 재동의 요청 필요

                    **응답 정보**
                    - reconsentRequired: 재동의 필요 여부
                    - pendingTerms: 재동의가 필요한 약관 목록

                    **처리 흐름**
                    1. 로그인 시 동의 상태 확인
                    2. 재동의 필요 시 약관 동의 페이지로 이동
                    3. 재동의 처리 후 서비스 이용 진행

                    **사용 화면**: 로그인 후 초기 화면, 마이페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 동의 상태 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "재동의 필요",
                                            value = """
                                                    {
                                                      "code": "T002",
                                                      "httpStatus": "OK",
                                                      "message": "약관 조회 성공",
                                                      "data": {
                                                        "reconsentRequired": true,
                                                        "pendingTerms": [
                                                          {
                                                            "id": "term789",
                                                            "type": "PRIVACY",
                                                            "version": 2,
                                                            "content": "개인정보 처리방침 v2 내용...",
                                                            "isRequired": true,
                                                            "effectiveDate": "2025-06-01"
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "재동의 불필요",
                                            value = """
                                                    {
                                                      "code": "T002",
                                                      "httpStatus": "OK",
                                                      "message": "약관 조회 성공",
                                                      "data": {
                                                        "reconsentRequired": false,
                                                        "pendingTerms": []
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getConsentStatus(
            @Parameter(hidden = true)
            String userId
    );
}
