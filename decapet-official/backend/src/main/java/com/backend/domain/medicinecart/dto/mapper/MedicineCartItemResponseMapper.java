package com.backend.domain.medicinecart.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.medicinecart.dto.response.MedicineCartItemResponse;
import com.backend.domain.medicinecart.entity.MedicineCartItem;

@Component
public class MedicineCartItemResponseMapper {

    public MedicineCartItemResponse toResponse(MedicineCartItem item) {
        return new MedicineCartItemResponse(
                item.getItemType(),
                item.getItemId(),
                item.getMedicineName(),
                item.getPetId(),
                item.getPetName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getImageUrl(),
                item.getQuestionnaireAnswers()
        );
    }
}
