package com.backend.domain.pet.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.pet.dto.request.PetVetRequest;
import com.backend.domain.pet.entity.Gender;

public record PetRegisterInfo(
        String userId,
        String name,
        String breedId,
        String customBreed,
        Gender gender,
        boolean neutered,
        LocalDate birthdate,
        BigDecimal weight,
        String registrationNumber,
        MultipartFile photo,
        List<PetVetRequest> vets
) {
}
