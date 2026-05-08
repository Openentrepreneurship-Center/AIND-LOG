package com.backend.domain.pet.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.breed.entity.Breed;
import com.backend.domain.pet.dto.internal.PetRegisterInfo;
import com.backend.domain.pet.dto.internal.PetUpdateInfo;
import com.backend.domain.pet.dto.request.PetRegisterRequest;
import com.backend.domain.pet.dto.request.PetUpdateRequest;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.entity.User;

@Component
public class PetMapper {

    public PetRegisterInfo toPetRegisterInfo(String userId, PetRegisterRequest request, MultipartFile photo) {
        return new PetRegisterInfo(
                userId,
                request.name(),
                request.breedId(),
                request.customBreed(),
                request.gender(),
                request.neutered(),
                request.birthdate(),
                request.weight(),
                request.registrationNumber(),
                photo,
                request.vets()
        );
    }

    public Pet toEntity(PetRegisterInfo info, User user, Breed breed) {
        return Pet.builder()
                .user(user)
                .name(info.name())
                .breed(breed)
                .customBreed(info.customBreed())
                .gender(info.gender())
                .neutered(info.neutered())
                .birthdate(info.birthdate())
                .weight(info.weight())
                .registrationNumber(info.registrationNumber())
                .build();
    }

    public PetUpdateInfo toPetUpdateInfo(String userId, String petId, PetUpdateRequest request, MultipartFile photo) {
        return new PetUpdateInfo(
                userId,
                petId,
                request.name(),
                request.breedId(),
                request.customBreed(),
                request.gender(),
                request.neutered(),
                request.birthdate(),
                request.weight(),
                request.registrationNumber(),
                photo,
                request.vets()
        );
    }
}
