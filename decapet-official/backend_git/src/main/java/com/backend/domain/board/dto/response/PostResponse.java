package com.backend.domain.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.board.entity.PostType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 정보 응답")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "post123")
        String id,

        @Schema(description = "작성자 사용자 ID", example = "user456")
        String userId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String userName,

        @Schema(description = "작성자 이메일", example = "user@example.com")
        String userEmail,

        @Schema(description = "작성자 전화번호", example = "01012345678")
        String userPhone,

        @Schema(description = "제보 유형", example = "DAMAGE_REPORT")
        PostType type,

        @Schema(description = "게시글 제목", example = "제품 문의드립니다")
        String title,

        @Schema(description = "게시글 내용", example = "제품 사용 중 궁금한 점이 있어서 문의합니다...")
        String content,

        @Schema(description = "첨부 파일 URL 목록")
        List<String> attachmentUrls,

        @Schema(description = "게시글 작성 일시", example = "2025-12-16T10:30:00")
        LocalDateTime createdAt
) {
}
