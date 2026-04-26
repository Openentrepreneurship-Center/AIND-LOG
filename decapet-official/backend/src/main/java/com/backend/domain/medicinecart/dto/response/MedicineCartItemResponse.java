package com.backend.domain.medicinecart.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.backend.domain.appointment.entity.QuestionnaireAnswer;
import com.backend.domain.medicinecart.entity.MedicineItemType;

public record MedicineCartItemResponse(
        MedicineItemType itemType,
        String itemId,
        String itemName,
        String petId,
        String petName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        String imageUrl,
        List<QuestionnaireAnswer> questionnaireAnswers
) {
}
