package com.backend.domain.pet.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.pet.dto.request.PetRegisterRequest;
import com.backend.domain.pet.dto.request.PetUpdateRequest;
import com.backend.domain.pet.dto.response.PetResponse;
import com.backend.domain.pet.mapper.PetMapper;
import com.backend.domain.pet.service.PetService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController implements PetApi {

    private final PetService petService;
    private final PetMapper petMapper;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> registerPet(
            @AuthenticationPrincipal String userId,
            @RequestPart("data") @Valid PetRegisterRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        PetResponse pet = petService.registerPet(petMapper.toPetRegisterInfo(userId, request, photo));
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PET_REGISTER_SUCCESS, pet));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMyPets(
            @AuthenticationPrincipal String userId) {
        List<PetResponse> pets = petService.getUserPets(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PET_LIST_SUCCESS, pets));
    }

    @Override
    @GetMapping("/{petId}")
    public ResponseEntity<SuccessResponse> getPet(
            @AuthenticationPrincipal String userId,
            @PathVariable String petId) {
        PetResponse pet = petService.getPet(userId, petId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PET_GET_SUCCESS, pet));
    }

    @Override
    @PatchMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updatePet(
            @AuthenticationPrincipal String userId,
            @PathVariable String petId,
            @RequestPart("data") @Valid PetUpdateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        PetResponse pet = petService.updatePet(petMapper.toPetUpdateInfo(userId, petId, request, photo));
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PET_UPDATE_SUCCESS, pet));
    }

    @Override
    @DeleteMapping("/{petId}")
    public ResponseEntity<SuccessResponse> deletePet(
            @AuthenticationPrincipal String userId,
            @PathVariable String petId) {
        petService.deletePet(userId, petId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PET_DELETE_SUCCESS));
    }
}
