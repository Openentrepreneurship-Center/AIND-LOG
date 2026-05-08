package com.backend.domain.sitesetting.controller;

import org.springframework.http.ResponseEntity;

import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사이트 설정", description = "사이트 설정 조회 (공개 API)")
public interface SiteSettingApi {

    @Operation(
            summary = "사이트 설정 조회",
            description = """
                    사이트 설정 정보를 조회합니다.

                    **조회 항목**
                    - 상단 배너: 문구, 노출 여부
                    - 회사 정보: HTML (리치 텍스트)
                    - 섹션 타이틀: 상품, 의약품

                    **권한**: 인증 불필요 (공개 API)

                    **사용 화면**: 메인 페이지, 푸터
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
}
