package com.backend.domain.medicinecart.dto.internal;

public record AddPrescriptionCommand(
    String prescriptionId,
    String timeSlotId
) {}
