package com.backend.domain.prescription.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
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

import com.backend.domain.prescription.dto.request.PrescriptionApproveRequest;
import com.backend.domain.prescription.dto.response.PrescriptionResponse;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.service.AdminPrescriptionService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/prescriptions")
@RequiredArgsConstructor
public class AdminPrescriptionController implements AdminPrescriptionApi {

    private final AdminPrescriptionService adminPrescriptionService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getPrescriptions(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        PageResponse<PrescriptionResponse> response;
        if (keyword != null && !keyword.isBlank()) {
            response = adminPrescriptionService.searchPrescriptions(status, keyword.trim(), pageable);
        } else if (status != null) {
            response = adminPrescriptionService.getPrescriptionsByStatus(status, pageable);
        } else {
            response = adminPrescriptionService.getAllPrescriptions(pageable);
        }
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{prescriptionId}")
    public ResponseEntity<SuccessResponse> getPrescription(@PathVariable String prescriptionId) {
        PrescriptionResponse response = adminPrescriptionService.getPrescription(prescriptionId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_GET_SUCCESS, response));
    }

    @Override
    @PostMapping(value = "/{prescriptionId}/approve", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse> approvePrescription(
            @PathVariable String prescriptionId,
            @RequestPart("data") @Valid PrescriptionApproveRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PrescriptionResponse response = adminPrescriptionService.approvePrescription(prescriptionId, request.price(), image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PRESCRIPTION_APPROVE_SUCCESS, response));
    }

    @Override
    @PostMapping("/{prescriptionId}/reject")
    public ResponseEntity<SuccessResponse> rejectPrescription(@PathVariable String prescriptionId) {
        PrescriptionResponse response = adminPrescriptionService.rejectPrescription(prescriptionId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PRESCRIPTION_REJECT_SUCCESS, response));
    }

    @Override
    @PostMapping("/{prescriptionId}/revert")
    public ResponseEntity<SuccessResponse> revertPrescription(@PathVariable String prescriptionId) {
        PrescriptionResponse response = adminPrescriptionService.revertPrescription(prescriptionId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PRESCRIPTION_REVERT_SUCCESS, response));
    }

    @DeleteMapping("/{prescriptionId}")
    public ResponseEntity<SuccessResponse> deletePrescription(@PathVariable String prescriptionId) {
        adminPrescriptionService.deletePrescription(prescriptionId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }

    @PutMapping(value = "/{prescriptionId}", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse> updatePrescription(
            @PathVariable String prescriptionId,
            @RequestParam(required = false) String medicineName,
            @RequestParam(required = false) BigDecimal price,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PrescriptionResponse response = adminPrescriptionService.updatePrescription(prescriptionId, medicineName, price, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }
}
