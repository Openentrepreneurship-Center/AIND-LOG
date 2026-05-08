package com.backend.domain.payment.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.service.AdminPaymentService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @GetMapping
    public ResponseEntity<SuccessResponse> getPayments(Pageable pageable) {
        PageResponse<PaymentResponse> response = adminPaymentService.getAllPayments(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PAYMENT_LIST_SUCCESS, response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = adminPaymentService.getPayment(paymentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_GET_SUCCESS, response));
    }

    @PostMapping("/{paymentId}/confirm-deposit")
    public ResponseEntity<SuccessResponse> confirmDeposit(@PathVariable String paymentId) {
        PaymentResponse response = adminPaymentService.confirmDeposit(paymentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PAYMENT_CONFIRM_DEPOSIT_SUCCESS, response));
    }
}
