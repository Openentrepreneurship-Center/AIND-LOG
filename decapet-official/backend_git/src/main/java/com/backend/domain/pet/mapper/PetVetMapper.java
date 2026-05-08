package com.backend.domain.pet.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.pet.dto.request.PetVetRequest;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.entity.PetVet;

@Component
public class PetVetMapper {

    public PetVet toEntity(PetVetRequest request, Pet pet) {
        return PetVet.builder()
                .pet(pet)
                .hospitalName(request.hospitalName())
                .vetName(request.vetName())
                .vetPosition(request.vetPosition())
                .build();
    }
}
