package com.backend.domain.pet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.backend.domain.breed.entity.Species;
import com.backend.domain.pet.entity.Gender;

public record PetResponse(
        String id,
        String uniqueNumber,
        String name,
        Species species,
        String breedId,
        String breedName,
        Gender gender,
        boolean neutered,
        LocalDate birthdate,
        BigDecimal weight,
        String registrationNumber,
        String photo,
        List<PetVetResponse> vets
) {
}
