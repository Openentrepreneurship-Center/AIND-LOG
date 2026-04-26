package com.backend.domain.prescription.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.prescription.dto.response.PrescriptionResponse;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.user.entity.User;

@Component
public class PrescriptionResponseMapper {

    public PrescriptionResponse toResponse(Prescription prescription) {
        Pet pet = prescription.getPet();
        User user = prescription.getUser();
        return new PrescriptionResponse(
                prescription.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                pet != null ? pet.getId() : null,
                pet != null ? pet.getName() : "(삭제된 반려동물)",
                pet != null ? pet.getGender() : null,
                pet != null ? pet.getBirthdate() : null,
                pet != null ? pet.getWeight() : null,
                prescription.getType(),
                prescription.getDescription(),
                prescription.getAttachmentUrls(),
                prescription.getMedicineName(),
                prescription.getMedicineWeight(),
                prescription.getDosageFrequency(),
                prescription.getDosagePeriod(),
                prescription.getAdditionalNotes(),
                prescription.getStatus(),
                prescription.getPrice(),
                prescription.getImageUrl(),
                prescription.getCreatedAt()
        );
    }
}
