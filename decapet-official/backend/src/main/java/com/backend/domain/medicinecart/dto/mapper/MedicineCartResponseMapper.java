package com.backend.domain.medicinecart.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.medicinecart.dto.response.MedicineCartItemResponse;
import com.backend.domain.medicinecart.dto.response.MedicineCartResponse;
import com.backend.domain.medicinecart.dto.response.MedicineCartResponse.TimeSlotInfo;
import com.backend.domain.medicinecart.entity.MedicineCart;
import com.backend.domain.schedule.entity.TimeSlot;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicineCartResponseMapper {

    private final MedicineCartItemResponseMapper medicineCartItemResponseMapper;

    public MedicineCartResponse toResponse(MedicineCart cart) {
        List<MedicineCartItemResponse> items = cart.getItems().stream()
                .map(medicineCartItemResponseMapper::toResponse)
                .toList();

        TimeSlotInfo timeSlotInfo = null;
        TimeSlot timeSlot = cart.getTimeSlot();
        if (timeSlot != null) {
            timeSlotInfo = new TimeSlotInfo(
                    timeSlot.getId(),
                    timeSlot.getSchedule().getId(),
                    timeSlot.getSchedule().getScheduleDate(),
                    timeSlot.getTime(),
                    timeSlot.getLocation()
            );
        }

        return new MedicineCartResponse(
                cart.getId(),
                cart.getTimeSlotId(),
                timeSlotInfo,
                items,
                cart.getTotalAmount(),
                cart.getTotalItemCount(),
                cart.isEmpty()
        );
    }
}
