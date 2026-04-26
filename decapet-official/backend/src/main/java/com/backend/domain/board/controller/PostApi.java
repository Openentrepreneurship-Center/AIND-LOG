package com.backend.domain.board.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.board.dto.request.CreatePostRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "게시판", description = "게시글 조회 및 작성")
@SecurityRequirement(name = "cookieAuth")
public interface PostApi {

    @Operation(
            summary = "게시글 작성",
            description = """
                    새로운 게시글을 작성합니다.

                    **제보 유형**
                    1. DAMAGE_REPORT (피해 업로드)
                       - 피해 사례를 신고하는 게시글
                    2. UNFAIR_TREATMENT (부당한 진료)
                       - 부당한 진료 행위를 신고하는 게시글
                    3. RECEIPT_UPLOAD (영수증 업로드)
                       - 영수증 관련 문의 게시글
                    4. PRE_INPUT (사전 입력)
                       - 기타 사전 입력 게시글

                    **비즈니스 로직**
                    - multipart/form-data 형식으로 전송
                    - 작성 후 상태는 OPEN으로 설정
                    - 파일은 S3에 업로드 후 URL 저장

                    **제약사항**
                    - 제보 유형(type), 제목, 내용은 필수
                    - 파일 첨부는 선택사항이며, 최대 5개까지 가능
                    - 각 파일은 최대 10MB까지 업로드 가능

                    **처리 흐름**
                    1. 제보 유형 선택
                    2. 게시글 작성 (제목, 내용, 파일 첨부)
                    3. 관리자 확인 대기

                    **사용 화면**: 게시판 작성 페이지, 제보하기 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 작성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "B001",
                                      "httpStatus": "CREATED",
                                      "message": "게시글 작성 성공",
                                      "data": {
                                        "id": "post123",
                                        "userId": "user456",
                                        "type": "DAMAGE_REPORT",
                                        "title": "제품 문의드립니다",
                                        "content": "제품 사용 중 궁금한 점이 있어서 문의합니다...",
                                        "attachmentUrls": [
                                          "https://example.com/attachments/file1.jpg",
                                          "https://example.com/attachments/file2.pdf"
                                        ],
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파일 개수 초과, 파일 크기 초과, 허용되지 않는 파일 형식 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "파일 개수 초과",
                                            value = """
                                                    {
                                                      "code": "B004",
                                                      "errorMessage": "첨부파일은 최대 5개까지 가능합니다.",
                                                      "httpStatus": "BAD_REQUEST"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "파일 크기 초과",
                                            value = """
                                                    {
                                                      "code": "B005",
                                                      "errorMessage": "파일 크기는 10MB를 초과할 수 없습니다.",
                                                      "httpStatus": "BAD_REQUEST"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "허용되지 않는 파일 형식",
                                            value = """
                                                    {
                                                      "code": "B006",
                                                      "errorMessage": "허용되지 않는 파일 형식입니다.",
                                                      "httpStatus": "BAD_REQUEST"
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "신고센터 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U010",
                                      "errorMessage": "신고센터 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    @RequestBody(
            description = "게시글 작성 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
                    }
            )
    )
    ResponseEntity<SuccessResponse> createPost(
            @Parameter(hidden = true)
            String userId,
            @Parameter(description = "게시글 작성 정보 (JSON)", required = true)
            CreatePostRequest request,
            @Parameter(description = "첨부 파일 목록 (최대 5개, 각 파일 최대 10MB)")
            List<MultipartFile> files
    );
}
