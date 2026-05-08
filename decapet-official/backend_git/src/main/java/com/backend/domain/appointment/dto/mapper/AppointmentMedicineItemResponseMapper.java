package com.backend.domain.appointment.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.dto.response.AppointmentMedicineItemResponse;
import com.backend.domain.appointment.entity.AppointmentMedicineItem;

@Component
public class AppointmentMedicineItemResponseMapper {

    public AppointmentMedicineItemResponse toResponse(AppointmentMedicineItem item) {
        return new AppointmentMedicineItemResponse(
                item.getId(),
                item.getItemType(),
                item.getMedicineId(),
                item.getPrescriptionId(),
                item.getMedicineName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getImageUrl(),
                item.getQuestionnaireAnswers()
        );
    }
}
