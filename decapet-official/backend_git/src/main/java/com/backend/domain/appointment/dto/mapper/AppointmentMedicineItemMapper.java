package com.backend.domain.appointment.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.entity.AppointmentMedicineItem;
import com.backend.domain.medicinecart.entity.MedicineCartItem;

@Component
public class AppointmentMedicineItemMapper {

    public AppointmentMedicineItem toEntity(MedicineCartItem cartItem) {
        return AppointmentMedicineItem.builder()
                .itemType(cartItem.getItemType())
                .medicineId(cartItem.getMedicineId())
                .prescriptionId(cartItem.getPrescriptionId())
                .medicineName(cartItem.getMedicineName())
                .unitPrice(cartItem.getUnitPrice())
                .quantity(cartItem.getQuantity())
                .imageUrl(cartItem.getImageUrl())
                .questionnaireAnswers(cartItem.getQuestionnaireAnswers())
                .build();
    }

    public List<AppointmentMedicineItem> toEntities(List<MedicineCartItem> cartItems) {
        return cartItems.stream()
                .map(this::toEntity)
                .toList();
    }
}
