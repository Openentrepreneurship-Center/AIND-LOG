package com.backend.domain.pet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPetService {

    private final PetRepository petRepository;

    @Transactional
    public void deletePet(String petId) {
        Pet pet = petRepository.getById(petId);
        pet.delete();
    }
}
