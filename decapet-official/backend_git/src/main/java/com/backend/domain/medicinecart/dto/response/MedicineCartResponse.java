package com.backend.domain.medicinecart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MedicineCartResponse(
        String id,
        String timeSlotId,
        TimeSlotInfo timeSlot,
        List<MedicineCartItemResponse> items,
        BigDecimal totalAmount,
        int totalItemCount,
        boolean empty
) {

    public record TimeSlotInfo(
            String id,
            String scheduleId,
            LocalDate date,
            LocalTime time,
            String location
    ) {
    }
}
