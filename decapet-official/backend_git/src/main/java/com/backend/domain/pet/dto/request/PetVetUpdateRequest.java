package com.backend.domain.pet.dto.request;

import com.backend.domain.pet.entity.VetPosition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수의사 정보 수정 요청")
public record PetVetUpdateRequest(
        @Schema(description = "수의사 정보 ID (null이면 신규 추가, 값이 있으면 수정)", example = "507f1f77bcf86cd799439013")
        String id,

        @Schema(description = "병원명 (최대 255자)", example = "서울동물병원")
        @NotBlank(message = "병원명을 입력해주세요.")
        @Size(max = 255, message = "병원명은 255자를 초과할 수 없습니다.")
        String hospitalName,

        @Schema(description = "수의사명 (최대 100자)", example = "김수의")
        @NotBlank(message = "수의사명을 입력해주세요.")
        @Size(max = 100, message = "수의사명은 100자를 초과할 수 없습니다.")
        String vetName,

        @Schema(description = "수의사 직급", example = "DIRECTOR")
        VetPosition vetPosition,

        @Schema(description = "삭제 여부 (true면 삭제)", example = "false")
        boolean deleted
) {
}
