package com.backend.domain.pet.controller;

import org.springframework.http.ResponseEntity;

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

@Tag(name = "[Admin] 반려동물", description = "반려동물 관리")
public interface AdminPetApi {

    @Operation(
            summary = "반려동물 삭제",
            description = """
                    관리자가 반려동물을 삭제합니다.

                    **주의**: 이 작업은 soft delete로 처리됩니다.

                    **사용 화면**: 관리자 > 회원 관리 > 반려동물 삭제
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "반려동물 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD102",
                                      "httpStatus": "OK",
                                      "message": "삭제 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "반려동물을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P001",
                                      "errorMessage": "반려동물을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> deletePet(
            @Parameter(description = "반려동물 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9V") String petId
    );
}
