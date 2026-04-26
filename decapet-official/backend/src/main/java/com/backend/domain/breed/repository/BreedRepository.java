package com.backend.domain.breed.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.breed.entity.Breed;
import com.backend.domain.breed.exception.BreedNotFoundException;

public interface BreedRepository extends JpaRepository<Breed, String> {

    List<Breed> findAllByOrderBySpeciesAscNameAsc();

    Optional<Breed> findByName(String name);

    default Breed getById(String id) {
        return findById(id).orElseThrow(BreedNotFoundException::new);
    }
}
