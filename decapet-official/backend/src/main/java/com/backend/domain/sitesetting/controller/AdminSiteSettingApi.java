package com.backend.domain.sitesetting.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.sitesetting.dto.request.UpdateSiteSettingRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Admin] 사이트 설정", description = "사이트 설정 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminSiteSettingApi {

    @Operation(
            summary = "사이트 설정 조회",
            description = """
                    사이트 설정 정보를 조회합니다.

                    **조회 항목**
                    - 상단 배너: 문구, 노출 여부
                    - 회사 정보: HTML (리치 텍스트)
                    - 섹션 타이틀: 상품, 의약품

                    **사용 화면**: 관리자 > 사이트 설정
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사이트 설정 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "SS001",
                                      "httpStatus": "OK",
                                      "message": "사이트 설정 조회 성공",
                                      "data": {
                                        "id": "01JJSITESETTING000001",
                                        "bannerText": "데카펫은 회원가입 승인 후 이용 가능합니다.",
                                        "bannerEnabled": true,
                                        "companyInfoHtml": "<p><strong>주식회사 비트진컴패니언</strong></p>",
                                        "productSectionTitle": "사료",
                                        "medicineSectionTitle": "진료 예약"
                                      }
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사이트 설정을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "SS001",
                                      "errorMessage": "사이트 설정을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getSiteSetting();

    @Operation(
            summary = "사이트 설정 수정",
            description = """
                    사이트 설정 정보를 수정합니다.

                    **수정 가능 항목**
                    - 상단 배너: 문구, 노출 여부
                    - 회사 정보: HTML (리치 텍스트)
                    - 섹션 타이틀: 상품, 의약품

                    **주의사항**
                    - 모든 필드는 선택적으로 수정 가능
                    - null로 전송된 필드도 null로 저장됨

                    **사용 화면**: 관리자 > 사이트 설정 > 수정
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사이트 설정 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD101",
                                      "httpStatus": "OK",
                                      "message": "수정 성공",
                                      "data": {
                                        "id": "01JJSITESETTING000001",
                                        "bannerText": "수정된 배너 문구",
                                        "bannerEnabled": true,
                                        "companyInfoHtml": "<p><strong>주식회사 비트진컴패니언</strong></p>",
                                        "productSectionTitle": "사료",
                                        "medicineSectionTitle": "진료 예약"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
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
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사이트 설정을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "SS001",
                                      "errorMessage": "사이트 설정을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    @RequestBody(
            description = "사이트 설정 수정 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateSiteSettingRequest.class)
            )
    )
    ResponseEntity<SuccessResponse> updateSiteSetting(
            @Valid UpdateSiteSettingRequest request
    );
}
