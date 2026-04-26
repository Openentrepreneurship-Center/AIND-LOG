package com.backend.domain.board.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.board.entity.PostType;
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

@Tag(name = "[Admin] 게시판", description = "게시판 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminBoardApi {

    @Operation(
            summary = "게시글 목록 조회",
            description = """
                    모든 게시글 목록을 조회합니다.

                    **필터링**
                    - type: 게시글 유형 (DAMAGE_REPORT, UNFAIR_TREATMENT, RECEIPT_UPLOAD, PRE_INPUT)

                    **정렬 및 페이징**
                    - 기본 정렬: 최신순 (createdAt DESC)
                    - 페이지 크기 기본값: 10

                    **사용 화면**: 관리자 > 게시판 관리 > 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "B002",
                                      "httpStatus": "OK",
                                      "message": "게시글 목록 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "post123",
                                            "userId": "user456",
                                            "type": "DAMAGE_REPORT",
                                            "title": "제품 문의드립니다",
                                            "content": "제품 사용 중 궁금한 점이 있어서 문의합니다...",
                                            "attachmentUrls": [
                                              "https://example.com/attachments/file1.jpg"
                                            ],
                                            "createdAt": "2025-12-16T10:30:00"
                                          }
                                        ],
                                        "totalElements": 1,
                                        "totalPages": 1,
                                        "size": 10,
                                        "number": 0
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
            )
    })
    ResponseEntity<SuccessResponse> getAllPosts(
            @Parameter(description = "게시글 유형 필터 (DAMAGE_REPORT, UNFAIR_TREATMENT, RECEIPT_UPLOAD, PRE_INPUT)", example = "DAMAGE_REPORT")
            PostType type,
            @Parameter(description = "제목/내용 검색 키워드")
            String keyword,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "게시글 상세 조회",
            description = """
                    특정 게시글의 상세 정보를 조회합니다.

                    **응답 정보**
                    - 게시글 ID, 제목, 내용
                    - 작성자(userId) 정보
                    - 첨부 파일 URL 목록
                    - 게시글 작성 일시

                    **사용 화면**: 관리자 > 게시판 관리 > 상세
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "B003",
                                      "httpStatus": "OK",
                                      "message": "게시글 조회 성공",
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
                    responseCode = "404",
                    description = "게시글을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "B001",
                                      "errorMessage": "게시글을 찾을 수 없습니다.",
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getPost(
            @Parameter(description = "게시글 ID", required = true, example = "post123")
            String postId
    );

    @Operation(
            summary = "게시글 삭제",
            description = """
                    게시글을 삭제합니다.

                    **처리 흐름**
                    1. 게시글 데이터 삭제
                    2. 첨부 파일 S3에서 삭제
                    3. 관련 데이터 정리

                    **주의사항**
                    - 삭제된 게시글은 복구 불가
                    - 첨부 파일도 함께 삭제됨

                    **사용 화면**: 관리자 > 게시판 관리 > 삭제
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD060",
                                      "httpStatus": "OK",
                                      "message": "게시글 처리 완료",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "B001",
                                      "errorMessage": "게시글을 찾을 수 없습니다.",
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> deletePost(
            @Parameter(description = "게시글 ID", required = true, example = "post123")
            String postId
    );
}
