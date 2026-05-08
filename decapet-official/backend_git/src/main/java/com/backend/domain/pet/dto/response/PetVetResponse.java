package com.backend.domain.pet.dto.response;

import com.backend.domain.pet.entity.VetPosition;

public record PetVetResponse(
        String id,
        String hospitalName,
        String vetName,
        VetPosition vetPosition
) {
}
