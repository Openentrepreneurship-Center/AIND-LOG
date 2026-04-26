package com.backend.domain.breed.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.breed.dto.mapper.BreedResponseMapper;
import com.backend.domain.breed.dto.response.BreedResponse;
import com.backend.domain.breed.repository.BreedRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BreedService {

    private final BreedRepository breedRepository;
    private final BreedResponseMapper breedResponseMapper;

    public List<BreedResponse> getAllBreeds() {
        return breedRepository.findAllByOrderBySpeciesAscNameAsc()
                .stream()
                .map(breedResponseMapper::toResponse)
                .toList();
    }
}
