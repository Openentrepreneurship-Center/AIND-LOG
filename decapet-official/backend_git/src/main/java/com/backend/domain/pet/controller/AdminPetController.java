package com.backend.domain.pet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.pet.service.AdminPetService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/pets")
@RequiredArgsConstructor
public class AdminPetController implements AdminPetApi {

    private final AdminPetService adminPetService;

    @Override
    @DeleteMapping("/{petId}")
    public ResponseEntity<SuccessResponse> deletePet(@PathVariable String petId) {
        adminPetService.deletePet(petId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }
}
