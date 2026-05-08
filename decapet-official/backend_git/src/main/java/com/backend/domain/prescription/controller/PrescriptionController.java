package com.backend.domain.prescription.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.prescription.dto.request.CreatePrescriptionRequest;
import com.backend.domain.prescription.dto.response.PrescriptionResponse;
import com.backend.domain.prescription.service.PrescriptionService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController implements PrescriptionApi {

    private final PrescriptionService prescriptionService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createPrescription(
            @AuthenticationPrincipal String userId,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments,
            @RequestPart("data") @Valid CreatePrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.createPrescription(userId, request, attachments);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_REQUEST_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMyPrescriptions(
            @AuthenticationPrincipal String userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PrescriptionResponse> response = prescriptionService.getUserPrescriptions(userId, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{prescriptionId}")
    public ResponseEntity<SuccessResponse> getPrescription(
            @AuthenticationPrincipal String userId,
            @PathVariable String prescriptionId) {
        PrescriptionResponse response = prescriptionService.getPrescription(userId, prescriptionId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_GET_SUCCESS, response));
    }

    @Override
    @GetMapping("/approved")
    public ResponseEntity<SuccessResponse> getApprovedPrescriptions(
            @AuthenticationPrincipal String userId) {
        List<PrescriptionResponse> response = prescriptionService.getApprovedPrescriptions(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRESCRIPTION_LIST_SUCCESS, response));
    }
}
