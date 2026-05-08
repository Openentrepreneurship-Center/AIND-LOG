package com.backend.domain.medicine.dto.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.backend.domain.medicine.dto.response.FeaturedMedicineResponse;
import com.backend.domain.medicine.dto.response.MedicineDetailResponse;
import com.backend.domain.medicine.dto.response.MedicineListResponse;
import com.backend.domain.medicine.dto.response.MedicineResponse;
import com.backend.domain.medicine.dto.response.QuestionnaireItemResponse;
import com.backend.domain.medicine.entity.Medicine;

@Component
public class MedicineResponseMapper {

    public MedicineResponse toResponse(Medicine medicine) {
        List<QuestionnaireItemResponse> questionnaireResponses = null;
        if (medicine.getQuestionnaire() != null) {
            questionnaireResponses = medicine.getQuestionnaire().stream()
                    .map(item -> new QuestionnaireItemResponse(item.getQuestionId(), item.getQuestion()))
                    .toList();
        }

        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getDescription(),
                medicine.getPrice(),
                medicine.getType(),
                medicine.getMinWeight(),
                medicine.getMaxWeight(),
                medicine.getExpirationDate(),
                medicine.getImageUrl(),
                medicine.getMonthsPerUnit(),
                questionnaireResponses,
                medicine.getSalePrice(),
                medicine.getSalesStatus(),
                medicine.getAdditionalInfo(),
                medicine.getCreatedAt()
        );
    }

    public MedicineDetailResponse toDetailResponse(Medicine medicine) {
        List<QuestionnaireItemResponse> questionnaireResponses = null;
        if (medicine.getQuestionnaire() != null) {
            questionnaireResponses = medicine.getQuestionnaire().stream()
                    .map(item -> new QuestionnaireItemResponse(item.getQuestionId(), item.getQuestion()))
                    .toList();
        }

        return new MedicineDetailResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getDescription(),
                medicine.getPrice(),
                medicine.getSalePrice(),
                medicine.getType(),
                medicine.getExpirationDate(),
                medicine.getImageUrl(),
                medicine.getMonthsPerUnit(),
                questionnaireResponses
        );
    }

    public MedicineListResponse toListResponse(Page<Medicine> medicinePage) {
        List<MedicineResponse> medicines = medicinePage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new MedicineListResponse(
                medicines,
                medicinePage.getNumber(),
                medicinePage.getSize(),
                medicinePage.getTotalElements(),
                medicinePage.getTotalPages(),
                medicinePage.hasNext(),
                medicinePage.hasPrevious()
        );
    }

    public List<MedicineResponse> toResponseList(List<Medicine> medicines) {
        return medicines.stream()
                .map(this::toResponse)
                .toList();
    }

    public FeaturedMedicineResponse toFeaturedResponse(Medicine medicine) {
        return new FeaturedMedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getPrice(),
                medicine.getExpirationDate(),
                medicine.getImageUrl(),
                medicine.getSalePrice()
        );
    }

    public List<FeaturedMedicineResponse> toFeaturedResponseList(List<Medicine> medicines) {
        return medicines.stream()
                .map(this::toFeaturedResponse)
                .toList();
    }
}
