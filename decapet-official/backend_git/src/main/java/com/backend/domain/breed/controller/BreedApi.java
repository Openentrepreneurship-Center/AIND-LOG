package com.backend.domain.breed.controller;

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

@Tag(name = "품종", description = "품종 조회")
public interface BreedApi {

    @Operation(
            summary = "품종 목록 조회",
            description = """
                    등록된 모든 품종 목록을 조회합니다.

                    **비즈니스 로직**
                    - 강아지(DOG)와 고양이(CAT) 품종이 모두 포함
                    - 인증 없이 조회 가능한 공개 API

                    **응답 정보**
                    - 품종 ID, 이름, 종(DOG/CAT)

                    **사용 화면**: 반려동물 등록 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "품종 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BR001",
                                      "httpStatus": "OK",
                                      "message": "품종 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "breed123",
                                          "name": "골든 리트리버",
                                          "species": "DOG"
                                        },
                                        {
                                          "id": "breed456",
                                          "name": "페르시안",
                                          "species": "CAT"
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getBreeds();
}
