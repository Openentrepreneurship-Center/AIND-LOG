package com.backend.domain.pet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.pet.entity.PetVet;
import com.backend.domain.pet.exception.PetVetNotFoundException;

public interface PetVetRepository extends JpaRepository<PetVet, String> {

    Optional<PetVet> findByIdAndPetId(String id, String petId);

    default PetVet getByIdAndPetId(String id, String petId) {
        return findByIdAndPetId(id, petId).orElseThrow(PetVetNotFoundException::new);
    }
}
