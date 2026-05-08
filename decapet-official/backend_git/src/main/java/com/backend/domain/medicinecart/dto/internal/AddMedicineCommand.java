package com.backend.domain.medicinecart.dto.internal;

import java.util.List;

import com.backend.domain.medicinecart.dto.request.QuestionnaireAnswerRequest;

public record AddMedicineCommand(
        String petId,
        String medicineId,
        String timeSlotId,
        Integer quantity,
        List<QuestionnaireAnswerRequest> questionnaireAnswers
) {
}
