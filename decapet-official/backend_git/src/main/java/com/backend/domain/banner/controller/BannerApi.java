package com.backend.domain.banner.controller;

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

@Tag(name = "배너", description = "배너 조회")
public interface BannerApi {

    @Operation(
            summary = "메인 배너 목록 조회",
            description = """
                    메인 화면에 표시할 배너 목록을 조회합니다.

                    **비즈니스 로직**
                    - 현재 시간이 startAt과 endAt 사이에 있는 활성 배너만 반환
                    - 배너는 displayOrder 오름차순으로 정렬
                    - 인증 없이 조회 가능한 공개 API

                    **응답 정보**
                    - 배너 ID, 제목, 이미지 URL, 표시 순서

                    **사용 화면**: 메인 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "httpStatus": "OK",
                                      "message": "배너 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "banner123",
                                          "title": "신규 회원 가입 이벤트",
                                          "imageUrl": "https://example.com/banner1.jpg",
                                          "displayOrder": 1
                                        },
                                        {
                                          "id": "banner456",
                                          "title": "연말 특가 프로모션",
                                          "imageUrl": "https://example.com/banner2.jpg",
                                          "displayOrder": 2
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getMainBanners();
}
