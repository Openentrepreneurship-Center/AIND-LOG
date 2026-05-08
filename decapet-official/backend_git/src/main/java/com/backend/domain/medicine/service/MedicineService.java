package com.backend.domain.medicine.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.medicine.dto.mapper.MedicineResponseMapper;
import com.backend.domain.medicine.dto.response.FeaturedMedicineResponse;
import com.backend.domain.medicine.dto.response.MedicineDetailResponse;
import com.backend.domain.medicine.dto.response.MedicineListResponse;
import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicine.entity.MedicineSalesStatus;
import com.backend.domain.medicine.repository.MedicineRepository;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineResponseMapper medicineResponseMapper;
    private final PetRepository petRepository;

    @Transactional(readOnly = true)
    public List<FeaturedMedicineResponse> getFeaturedMedicines() {
        List<Medicine> medicines = medicineRepository.findRandomFeaturedMedicines();
        return medicineResponseMapper.toFeaturedResponseList(medicines);
    }

    @Transactional(readOnly = true)
    public MedicineListResponse getMedicines(String userId, String petId, Pageable pageable) {
        Page<Medicine> medicines;

        if (petId != null) {
            Pet pet = petRepository.getByIdAndUserId(petId, userId);
            BigDecimal weight = pet.getWeight();
            medicines = medicineRepository.findByWeightRange(weight, pageable);
        } else {
            medicines = medicineRepository.findBySalesStatus(MedicineSalesStatus.ON_SALE, pageable);
        }

        return medicineResponseMapper.toListResponse(medicines);
    }

    @Transactional(readOnly = true)
    public MedicineDetailResponse getMedicine(String medicineId) {
        Medicine medicine = medicineRepository.findByIdOrThrow(medicineId);
        return medicineResponseMapper.toDetailResponse(medicine);
    }

    @Transactional(readOnly = true)
    public Medicine getMedicineEntity(String medicineId) {
        return medicineRepository.findByIdOrThrow(medicineId);
    }
}
