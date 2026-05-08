package com.backend.domain.medicine.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.medicine.dto.request.CreateMedicineRequest;
import com.backend.domain.medicine.dto.request.UpdateMedicineRequest;
import com.backend.domain.medicine.dto.response.MedicineListResponse;
import com.backend.domain.medicine.dto.response.MedicineResponse;
import com.backend.domain.medicine.entity.MedicineType;
import com.backend.domain.medicine.service.AdminMedicineService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/medicines")
@RequiredArgsConstructor
public class AdminMedicineController implements AdminMedicineApi {

    private final AdminMedicineService adminMedicineService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createMedicine(
            @RequestPart("data") @Valid CreateMedicineRequest request,
            @RequestPart("image") MultipartFile image) {
        MedicineResponse response = adminMedicineService.createMedicine(request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping("/{medicineId}")
    public ResponseEntity<SuccessResponse> getMedicine(@PathVariable String medicineId) {
        MedicineResponse response = adminMedicineService.getMedicine(medicineId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_GET_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllMedicines(
            @RequestParam(required = false) MedicineType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "expirationDate", direction = Sort.Direction.ASC) Pageable pageable) {
        MedicineListResponse response = adminMedicineService.getAllMedicines(type, keyword, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_LIST_SUCCESS, response));
    }

    @Override
    @PutMapping(value = "/{medicineId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateMedicine(
            @PathVariable String medicineId,
            @RequestPart("data") @Valid UpdateMedicineRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        MedicineResponse response = adminMedicineService.updateMedicine(medicineId, request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{medicineId}")
    public ResponseEntity<SuccessResponse> deleteMedicine(@PathVariable String medicineId) {
        adminMedicineService.deleteMedicine(medicineId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_DELETE_SUCCESS));
    }
}
