package com.backend.domain.breed.dto.response;

import com.backend.domain.breed.entity.Species;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "품종 정보 응답")
public record BreedResponse(
        @Schema(description = "품종 ID", example = "breed123")
        String id,

        @Schema(description = "품종 이름", example = "골든 리트리버")
        String name,

        @Schema(description = "동물 종류 (DOG: 강아지, CAT: 고양이)", example = "DOG")
        Species species
) {
}
