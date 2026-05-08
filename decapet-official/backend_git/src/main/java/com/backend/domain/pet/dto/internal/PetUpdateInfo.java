package com.backend.domain.pet.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.pet.dto.request.PetVetUpdateRequest;
import com.backend.domain.pet.entity.Gender;

public record PetUpdateInfo(
        String userId,
        String petId,
        String name,
        String breedId,
        String customBreed,
        Gender gender,
        Boolean neutered,
        LocalDate birthdate,
        BigDecimal weight,
        String registrationNumber,
        MultipartFile photo,
        List<PetVetUpdateRequest> vets
) {
}
