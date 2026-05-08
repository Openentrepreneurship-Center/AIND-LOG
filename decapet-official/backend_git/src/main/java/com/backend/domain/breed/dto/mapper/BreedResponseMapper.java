package com.backend.domain.breed.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.breed.dto.response.BreedResponse;
import com.backend.domain.breed.entity.Breed;

@Component
public class BreedResponseMapper {

    public BreedResponse toResponse(Breed breed) {
        return new BreedResponse(
                breed.getId(),
                breed.getName(),
                breed.getSpecies()
        );
    }
}
