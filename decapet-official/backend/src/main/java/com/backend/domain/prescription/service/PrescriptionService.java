package com.backend.domain.prescription.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.backend.global.common.PageResponse;
import com.backend.global.service.S3Service;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.repository.PetRepository;
import com.backend.domain.prescription.dto.internal.PrescriptionCreateInfo;
import com.backend.domain.prescription.dto.mapper.PrescriptionMapper;
import com.backend.domain.prescription.dto.mapper.PrescriptionResponseMapper;
import com.backend.domain.prescription.dto.request.CreatePrescriptionRequest;
import com.backend.domain.prescription.dto.response.PrescriptionResponse;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.entity.PrescriptionType;
import com.backend.domain.prescription.exception.PrescriptionPermissionDeniedException;
import com.backend.domain.prescription.exception.PrescriptionValidationException;
import com.backend.domain.prescription.repository.PrescriptionRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionResponseMapper prescriptionResponseMapper;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public PrescriptionResponse createPrescription(String userId, CreatePrescriptionRequest request, List<MultipartFile> attachments) {
        User user = userRepository.getReferenceById(userId);
        Pet pet = petRepository.getByIdAndUserId(request.petId(), userId);

        validatePrescriptionRequest(request, attachments);

        List<String> attachmentUrls = uploadAttachments(request.type(), attachments);

        try {
            return transactionTemplate.execute(status -> {
                PrescriptionCreateInfo createInfo = new PrescriptionCreateInfo(
                        user,
                        pet,
                        request.type(),
                        request.description(),
                        attachmentUrls,
                        request.medicineName(),
                        request.medicineWeight(),
                        request.dosageFrequency(),
                        request.dosagePeriod(),
                        request.additionalNotes()
                );
                Prescription prescription = prescriptionMapper.toEntity(createInfo);
                Prescription saved = prescriptionRepository.save(prescription);
                return prescriptionResponseMapper.toResponse(saved);
            });
        } catch (Exception e) {
            s3Service.deleteFiles(attachmentUrls);
            throw e;
        }
    }

    private void validatePrescriptionRequest(CreatePrescriptionRequest request, List<MultipartFile> attachments) {
        if (request.type() == PrescriptionType.SIMPLE) {
            if (request.description() == null || request.description().isBlank()) {
                throw new PrescriptionValidationException("간단 신청에서는 설명이 필수입니다.");
            }
            if (attachments != null && !attachments.isEmpty()) {
                throw new PrescriptionValidationException("간단 신청에서는 첨부파일을 업로드할 수 없습니다.");
            }
        } else if (request.type() == PrescriptionType.DETAILED) {
            if (attachments == null || attachments.isEmpty()) {
                throw new PrescriptionValidationException("상세 신청에서는 최소 1개의 첨부파일이 필요합니다.");
            }
            if (attachments.size() > 4) {
                throw new PrescriptionValidationException("첨부파일은 최대 4개까지 업로드 가능합니다.");
            }
            if (request.medicineName() == null || request.medicineName().isBlank()) {
                throw new PrescriptionValidationException("상세 신청에서는 의약품명이 필수입니다.");
            }
            if (request.medicineWeight() == null || request.medicineWeight().isBlank()) {
                throw new PrescriptionValidationException("상세 신청에서는 의약품 무게가 필수입니다.");
            }
            if (request.dosageFrequency() == null || request.dosageFrequency().isBlank()) {
                throw new PrescriptionValidationException("상세 신청에서는 복용 횟수가 필수입니다.");
            }
            if (request.dosagePeriod() == null || request.dosagePeriod().isBlank()) {
                throw new PrescriptionValidationException("상세 신청에서는 복용 기간이 필수입니다.");
            }
        }
    }

    private List<String> uploadAttachments(PrescriptionType type, List<MultipartFile> attachments) {
        if (type == PrescriptionType.SIMPLE || attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }
        return s3Service.uploadFiles(attachments, "prescriptions");
    }

    public PageResponse<PrescriptionResponse> getUserPrescriptions(String userId, Pageable pageable) {
        return PageResponse.from(
            prescriptionRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(prescriptionResponseMapper::toResponse)
        );
    }

    public PrescriptionResponse getPrescription(String userId, String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        if (!prescription.getUser().getId().equals(userId)) {
            throw new PrescriptionPermissionDeniedException();
        }
        return prescriptionResponseMapper.toResponse(prescription);
    }

    public List<PrescriptionResponse> getApprovedPrescriptions(String userId) {
        return prescriptionRepository.findByUserIdAndStatusAndDeletedAtIsNull(
                userId, PrescriptionStatus.APPROVED
        ).stream()
         .map(prescriptionResponseMapper::toResponse)
         .toList();
    }
}
