package com.backend.domain.breed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.breed.dto.response.BreedResponse;
import com.backend.domain.breed.service.BreedService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/breeds")
@RequiredArgsConstructor
public class BreedController implements BreedApi {

    private final BreedService breedService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getBreeds() {
        List<BreedResponse> breeds = breedService.getAllBreeds();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.BREED_LIST_SUCCESS, breeds));
    }
}
