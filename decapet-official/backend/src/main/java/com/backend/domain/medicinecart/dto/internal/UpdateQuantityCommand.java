package com.backend.domain.medicinecart.dto.internal;

import com.backend.domain.medicinecart.entity.MedicineItemType;

public record UpdateQuantityCommand(
        String userId,
        MedicineItemType itemType,
        String petId,
        String itemId,
        int quantity
) {
}
