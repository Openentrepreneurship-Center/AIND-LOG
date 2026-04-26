package com.backend.domain.medicine.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.medicine.dto.request.CreateMedicineRequest;
import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicine.entity.MedicineSalesStatus;

@Component
public class MedicineMapper {

    public Medicine toEntity(CreateMedicineRequest request, String imageUrl) {
        return Medicine.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .type(request.type())
                .minWeight(request.minWeight())
                .maxWeight(request.maxWeight())
                .expirationDate(request.expirationDate())
                .imageUrl(imageUrl)
                .questionnaire(request.questionnaire())
                .monthsPerUnit(request.monthsPerUnit())
                .salePrice(request.salePrice())
                .salesStatus(request.salesStatus() != null ? request.salesStatus() : MedicineSalesStatus.ON_SALE)
                .additionalInfo(request.additionalInfo())
                .build();
    }
}
