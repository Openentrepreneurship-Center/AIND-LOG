package com.backend.domain.prescription.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.prescription.dto.internal.PrescriptionCreateInfo;
import com.backend.domain.prescription.entity.Prescription;

@Component
public class PrescriptionMapper {

    public Prescription toEntity(PrescriptionCreateInfo createInfo) {
        return Prescription.builder()
                .user(createInfo.user())
                .pet(createInfo.pet())
                .type(createInfo.type())
                .description(createInfo.description())
                .attachmentUrls(createInfo.attachmentUrls())
                .medicineName(createInfo.medicineName())
                .medicineWeight(createInfo.medicineWeight())
                .dosageFrequency(createInfo.dosageFrequency())
                .dosagePeriod(createInfo.dosagePeriod())
                .additionalNotes(createInfo.additionalNotes())
                .build();
    }
}
