package com.backend.domain.board.dto.request;

import com.backend.domain.board.entity.PostType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 작성 요청", example = """
        {
          "type": "DAMAGE_REPORT",
          "title": "제품 문의드립니다",
          "content": "제품 사용 중 궁금한 점이 있어서 문의합니다..."
        }""")
public record CreatePostRequest(
        @Schema(description = "제보 유형 (DAMAGE_REPORT: 피해 업로드, UNFAIR_TREATMENT: 부당한 진료, RECEIPT_UPLOAD: 영수증 업로드, PRE_INPUT: 사전 입력)",
                example = "DAMAGE_REPORT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "제보 유형을 선택해주세요.")
        PostType type,

        @Schema(description = "게시글 제목 (최대 200자)", example = "제품 문의드립니다", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "게시글 내용 (최대 5000자)", example = "제품 사용 중 궁금한 점이 있어서 문의합니다...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content

) {
}
