package com.backend.domain.medicinecart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.medicinecart.dto.internal.AddMedicineCommand;
import com.backend.domain.medicinecart.dto.internal.UpdateQuantityCommand;
import com.backend.domain.medicinecart.dto.request.AddMedicineRequest;
import com.backend.domain.medicinecart.dto.request.AddPrescriptionRequest;
import com.backend.domain.medicinecart.dto.request.UpdateQuantityRequest;
import com.backend.domain.medicinecart.dto.response.MedicineCartResponse;
import com.backend.domain.medicinecart.entity.MedicineItemType;
import com.backend.domain.medicinecart.service.MedicineCartService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medicine-carts")
@RequiredArgsConstructor
public class MedicineCartController implements MedicineCartApi {

    private final MedicineCartService cartService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getCart(
            @AuthenticationPrincipal String userId) {
        MedicineCartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_GET_SUCCESS, response));
    }

    @Override
    @PostMapping("/items")
    public ResponseEntity<SuccessResponse> addMedicine(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid AddMedicineRequest request) {
        AddMedicineCommand command = new AddMedicineCommand(
                request.petId(),
                request.medicineId(),
                request.timeSlotId(),
                request.quantity(),
                request.questionnaireAnswers()
        );
        MedicineCartResponse response = cartService.addMedicine(userId, command);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_ADD_SUCCESS, response));
    }

    @PostMapping("/items/prescription")
    public ResponseEntity<SuccessResponse> addPrescription(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid AddPrescriptionRequest request) {
        MedicineCartResponse response = cartService.addPrescription(userId, request.toCommand());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_ADD_SUCCESS, response));
    }

    @Override
    @PatchMapping("/items")
    public ResponseEntity<SuccessResponse> updateQuantity(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "MEDICINE") MedicineItemType itemType,
            @RequestParam String petId,
            @RequestParam String itemId,
            @RequestBody @Valid UpdateQuantityRequest request) {
        UpdateQuantityCommand command = new UpdateQuantityCommand(userId, itemType, petId, itemId, request.quantity());
        MedicineCartResponse response = cartService.updateQuantity(command);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/items")
    public ResponseEntity<SuccessResponse> removeMedicine(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "MEDICINE") MedicineItemType itemType,
            @RequestParam String petId,
            @RequestParam String itemId) {
        MedicineCartResponse response = cartService.removeMedicine(userId, itemType, petId, itemId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_REMOVE_SUCCESS, response));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<SuccessResponse> clearCart(
            @AuthenticationPrincipal String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.MEDICINE_CART_CLEAR_SUCCESS));
    }
}
