package com.backend.domain.prescription.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.global.common.PageResponse;
import com.backend.global.service.S3Service;

import com.backend.domain.prescription.dto.mapper.PrescriptionResponseMapper;
import com.backend.domain.prescription.dto.response.PrescriptionResponse;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionResponseMapper prescriptionResponseMapper;
    private final S3Service s3Service;

    public PageResponse<PrescriptionResponse> getPrescriptionsByStatus(PrescriptionStatus status, Pageable pageable) {
        return PageResponse.from(
            prescriptionRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                .map(prescriptionResponseMapper::toResponse)
        );
    }

    public PageResponse<PrescriptionResponse> getAllPrescriptions(Pageable pageable) {
        return PageResponse.from(
            prescriptionRepository.findByDeletedAtIsNull(pageable)
                .map(prescriptionResponseMapper::toResponse)
        );
    }

    public PrescriptionResponse getPrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        return prescriptionResponseMapper.toResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse approvePrescription(String prescriptionId, BigDecimal price, MultipartFile image) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadFile(image, "prescriptions");
        }
        prescription.approve(price, imageUrl);
        return prescriptionResponseMapper.toResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse rejectPrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        prescription.reject();
        return prescriptionResponseMapper.toResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse revertPrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        prescription.revertToPending();
        return prescriptionResponseMapper.toResponse(prescription);
    }

    @Transactional
    public void deletePrescription(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        prescription.delete();
    }

    @Transactional
    public PrescriptionResponse updatePrescription(String prescriptionId, String medicineName, BigDecimal price, MultipartFile image) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        if (medicineName != null) {
            prescription.updateMedicineName(medicineName);
        }
        if (price != null) {
            prescription.updatePrice(price);
        }
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, "prescriptions");
            prescription.updateImageUrl(imageUrl);
        }
        return prescriptionResponseMapper.toResponse(prescription);
    }

    public PageResponse<PrescriptionResponse> searchPrescriptions(PrescriptionStatus status, String keyword, Pageable pageable) {
        return PageResponse.from(
            prescriptionRepository.searchByKeyword(status, keyword, pageable)
                .map(prescriptionResponseMapper::toResponse)
        );
    }
}
