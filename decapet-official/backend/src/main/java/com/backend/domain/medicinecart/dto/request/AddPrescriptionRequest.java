package com.backend.domain.medicinecart.dto.request;

import com.backend.domain.medicinecart.dto.internal.AddPrescriptionCommand;

import jakarta.validation.constraints.NotBlank;

public record AddPrescriptionRequest(
    @NotBlank(message = "처방 ID는 필수입니다.")
    String prescriptionId,

    @NotBlank(message = "타임슬롯 ID는 필수입니다.")
    String timeSlotId
) {
    public AddPrescriptionCommand toCommand() {
        return new AddPrescriptionCommand(prescriptionId, timeSlotId);
    }
}
