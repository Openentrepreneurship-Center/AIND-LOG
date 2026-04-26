package com.backend.domain.medicine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.medicine.dto.mapper.MedicineMapper;
import com.backend.domain.medicine.dto.mapper.MedicineResponseMapper;
import com.backend.domain.medicine.dto.request.CreateMedicineRequest;
import com.backend.domain.medicine.dto.request.UpdateMedicineRequest;
import com.backend.domain.medicine.dto.response.MedicineListResponse;
import com.backend.domain.medicine.dto.response.MedicineResponse;
import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicine.entity.MedicineType;
import com.backend.domain.medicine.exception.MonthsPerUnitNotAllowedException;
import com.backend.domain.medicine.exception.MonthsPerUnitRequiredException;
import com.backend.domain.medicine.repository.MedicineRepository;
import com.backend.global.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMedicineService {

    private static final String MEDICINE_IMAGE_DIRECTORY = "medicines";

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final MedicineResponseMapper medicineResponseMapper;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;

    public MedicineResponse createMedicine(CreateMedicineRequest request, MultipartFile image) {
        validateMonthsPerUnit(request.type(), request.monthsPerUnit());

        String imageUrl = s3Service.uploadFile(image, MEDICINE_IMAGE_DIRECTORY);

        try {
            return transactionTemplate.execute(status -> {
                Medicine medicine = medicineMapper.toEntity(request, imageUrl);
                Medicine savedMedicine = medicineRepository.save(medicine);
                return medicineResponseMapper.toResponse(savedMedicine);
            });
        } catch (Exception e) {
            s3Service.deleteFile(imageUrl);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public MedicineResponse getMedicine(String medicineId) {
        Medicine medicine = medicineRepository.findByIdOrThrow(medicineId);
        return medicineResponseMapper.toResponse(medicine);
    }

    @Transactional(readOnly = true)
    public MedicineListResponse getAllMedicines(MedicineType type, Pageable pageable) {
        Page<Medicine> medicines = type != null
                ? medicineRepository.findByType(type, pageable)
                : medicineRepository.findAll(pageable);
        return medicineResponseMapper.toListResponse(medicines);
    }

    @Transactional(readOnly = true)
    public MedicineListResponse getAllMedicines(MedicineType type, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getAllMedicines(type, pageable);
        }

        Page<Medicine> medicines = type != null
                ? medicineRepository.findByTypeAndNameContainingIgnoreCase(type, keyword, pageable)
                : medicineRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return medicineResponseMapper.toListResponse(medicines);
    }

    public MedicineResponse updateMedicine(String medicineId, UpdateMedicineRequest request, MultipartFile image) {
        Medicine medicine = medicineRepository.findByIdOrThrow(medicineId);
        validateMonthsPerUnit(request.type(), request.monthsPerUnit());

        String oldImageUrl = medicine.getImageUrl();
        String newImageUrl = (image != null && !image.isEmpty())
                ? s3Service.uploadFile(image, MEDICINE_IMAGE_DIRECTORY)
                : null;

        try {
            MedicineResponse response = transactionTemplate.execute(status -> {
                String imageUrl = newImageUrl != null ? newImageUrl : oldImageUrl;
                medicine.update(
                        request.name(),
                        request.description(),
                        request.price(),
                        request.type(),
                        request.minWeight(),
                        request.maxWeight(),
                        request.expirationDate(),
                        imageUrl,
                        request.questionnaire(),
                        request.monthsPerUnit(),
                        request.salePrice(),
                        request.salesStatus() != null ? request.salesStatus() : medicine.getSalesStatus(),
                        request.additionalInfo()
                );
                return medicineResponseMapper.toResponse(medicine);
            });

            if (newImageUrl != null && oldImageUrl != null) {
                s3Service.deleteFile(oldImageUrl);
            }
            return response;
        } catch (Exception e) {
            if (newImageUrl != null) {
                s3Service.deleteFile(newImageUrl);
            }
            throw e;
        }
    }

    @Transactional
    public void deleteMedicine(String medicineId) {
        Medicine medicine = medicineRepository.findByIdOrThrow(medicineId);
        String imageUrl = medicine.getImageUrl();
        medicine.delete();

        if (imageUrl != null) {
            s3Service.deleteFile(imageUrl);
        }
    }

    private void validateMonthsPerUnit(MedicineType type, Integer monthsPerUnit) {
        if (type == MedicineType.PARASITE && monthsPerUnit == null) {
            throw new MonthsPerUnitRequiredException();
        }
        if (type != MedicineType.PARASITE && monthsPerUnit != null) {
            throw new MonthsPerUnitNotAllowedException();
        }
    }
}
