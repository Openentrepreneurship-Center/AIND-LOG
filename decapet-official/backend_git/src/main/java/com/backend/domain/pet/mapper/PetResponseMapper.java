package com.backend.domain.pet.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.pet.dto.response.PetResponse;
import com.backend.domain.pet.entity.Pet;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PetResponseMapper {

    private final PetVetResponseMapper petVetResponseMapper;

    public PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getUniqueNumber(),
                pet.getName(),
                pet.getBreed().getSpecies(),
                pet.getBreed().getId(),
                pet.getBreedName(),
                pet.getGender(),
                pet.getNeutered() != null && pet.getNeutered(),
                pet.getBirthdate(),
                pet.getWeight(),
                pet.getRegistrationNumber(),
                pet.getPhoto(),
                petVetResponseMapper.toResponses(pet.getVets())
        );
    }
}
