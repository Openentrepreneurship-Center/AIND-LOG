package com.backend.domain.banner.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "배너 순서 변경 요청", example = """
        {
          "bannerIds": ["banner456", "banner123", "banner789"]
        }""")
public record ReorderBannersRequest(
        @Schema(description = "변경할 배너 ID 목록 (순서대로)", example = "[\"banner456\", \"banner123\", \"banner789\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "배너 ID 목록을 입력해주세요.")
        List<String> bannerIds
) {
}
