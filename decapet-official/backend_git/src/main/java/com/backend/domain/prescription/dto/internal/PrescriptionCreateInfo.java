package com.backend.domain.prescription.dto.internal;

import java.util.List;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.prescription.entity.PrescriptionType;
import com.backend.domain.user.entity.User;

public record PrescriptionCreateInfo(
	User user,
	Pet pet,
	PrescriptionType type,
	String description,
	List<String> attachmentUrls,
	String medicineName,
	String medicineWeight,
	String dosageFrequency,
	String dosagePeriod,
	String additionalNotes
) {
}
