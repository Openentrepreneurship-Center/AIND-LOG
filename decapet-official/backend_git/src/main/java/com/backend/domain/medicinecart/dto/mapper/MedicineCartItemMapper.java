package com.backend.domain.medicinecart.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.entity.QuestionnaireAnswer;
import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicinecart.dto.request.QuestionnaireAnswerRequest;
import com.backend.domain.medicinecart.entity.MedicineCartItem;
import com.backend.domain.medicinecart.entity.MedicineItemType;
import com.backend.domain.prescription.entity.Prescription;

@Component
public class MedicineCartItemMapper {

    public MedicineCartItem toEntity(Medicine medicine, int quantity,
            String petId, String petName, List<QuestionnaireAnswerRequest> answerRequests) {
        List<QuestionnaireAnswer> answers = mapAnswers(answerRequests);

        return MedicineCartItem.builder()
                .itemType(MedicineItemType.MEDICINE)
                .medicineId(medicine.getId())
                .itemName(medicine.getName())
                .petId(petId)
                .petName(petName)
                .imageUrl(medicine.getImageUrl())
                .unitPrice(medicine.getSalePrice() != null ? medicine.getSalePrice() : medicine.getPrice())
                .quantity(quantity)
                .questionnaireAnswers(answers)
                .build();
    }

    public MedicineCartItem toPrescriptionEntity(Prescription prescription) {
        String itemName = prescription.getMedicineName() != null
                ? prescription.getMedicineName()
                : "처방 의약품";

        return MedicineCartItem.builder()
                .itemType(MedicineItemType.PRESCRIPTION)
                .prescriptionId(prescription.getId())
                .itemName(itemName)
                .petId(prescription.getPet().getId())
                .petName(prescription.getPet().getName())
                .imageUrl(null)
                .unitPrice(prescription.getPrice())
                .quantity(1)
                .questionnaireAnswers(null)
                .build();
    }

    private List<QuestionnaireAnswer> mapAnswers(List<QuestionnaireAnswerRequest> answerRequests) {
        if (answerRequests == null) {
            return null;
        }

        return answerRequests.stream()
                .map(this::toQuestionnaireAnswer)
                .toList();
    }

    private QuestionnaireAnswer toQuestionnaireAnswer(QuestionnaireAnswerRequest request) {
        return QuestionnaireAnswer.builder()
                .questionId(request.questionId())
                .answer(request.answer())
                .build();
    }
}
