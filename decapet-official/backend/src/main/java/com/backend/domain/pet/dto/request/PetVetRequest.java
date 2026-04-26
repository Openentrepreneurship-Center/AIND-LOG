package com.backend.domain.pet.dto.request;

import com.backend.domain.pet.entity.VetPosition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수의사 정보 요청")
public record PetVetRequest(
        @Schema(description = "병원명 (최대 50자)", example = "서울동물병원")
        @NotBlank(message = "병원명을 입력해주세요.")
        @Size(max = 50, message = "병원명은 50자를 초과할 수 없습니다.")
        String hospitalName,

        @Schema(description = "수의사명 (최대 5자)", example = "김수의")
        @NotBlank(message = "수의사명을 입력해주세요.")
        @Size(max = 5, message = "수의사명은 5자를 초과할 수 없습니다.")
        String vetName,

        @Schema(description = "수의사 직급 (DIRECTOR, MANAGER, STAFF 등)", example = "DIRECTOR")
        VetPosition vetPosition
) {
}
