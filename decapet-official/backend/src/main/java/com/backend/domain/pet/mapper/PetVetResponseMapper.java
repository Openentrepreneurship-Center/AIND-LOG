package com.backend.domain.pet.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.pet.dto.response.PetVetResponse;
import com.backend.domain.pet.entity.PetVet;

@Component
public class PetVetResponseMapper {

    public PetVetResponse toResponse(PetVet petVet) {
        return new PetVetResponse(
                petVet.getId(),
                petVet.getHospitalName(),
                petVet.getVetName(),
                petVet.getVetPosition()
        );
    }

    public List<PetVetResponse> toResponses(List<PetVet> vets) {
        return vets.stream()
                .map(this::toResponse)
                .toList();
    }
}
