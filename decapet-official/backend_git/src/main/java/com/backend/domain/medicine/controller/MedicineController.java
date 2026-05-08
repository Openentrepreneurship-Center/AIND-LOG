package com.backend.domain.medicine.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.medicine.dto.response.FeaturedMedicineResponse;
import com.backend.domain.medicine.dto.response.MedicineDetailResponse;
import com.backend.domain.medicine.dto.response.MedicineListResponse;
import com.backend.domain.medicine.service.MedicineService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController implements MedicineApi {

    private final MedicineService medicineService;

    @Override
    @GetMapping("/featured")
    public ResponseEntity<SuccessResponse> getFeaturedMedicines() {
        List<FeaturedMedicineResponse> response = medicineService.getFeaturedMedicines();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMedicines(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) String petId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        MedicineListResponse response = medicineService.getMedicines(userId, petId, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{medicineId}")
    public ResponseEntity<SuccessResponse> getMedicine(@PathVariable String medicineId) {
        MedicineDetailResponse response = medicineService.getMedicine(medicineId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_GET_SUCCESS, response));
    }
}
