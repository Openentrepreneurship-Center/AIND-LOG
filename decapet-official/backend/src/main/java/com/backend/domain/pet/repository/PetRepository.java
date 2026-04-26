package com.backend.domain.pet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.exception.PetNotFoundException;

public interface PetRepository extends JpaRepository<Pet, String> {

    List<Pet> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Pet> findByIdAndUserId(String id, String userId);

	default Pet getByIdAndUserId(String petId, String userId) {
		return findByIdAndUserId(petId, userId).orElseThrow(PetNotFoundException::new);
	}

	default Pet getById(String petId) {
		return findById(petId).orElseThrow(PetNotFoundException::new);
	}
}
