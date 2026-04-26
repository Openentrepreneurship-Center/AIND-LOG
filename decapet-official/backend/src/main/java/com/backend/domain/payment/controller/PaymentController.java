package com.backend.domain.payment.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.payment.dto.request.CompletePaymentRequest;
import com.backend.domain.payment.dto.request.CreatePaymentRequest;
import com.backend.domain.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.service.PaymentService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> createPayment(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_CREATE_SUCCESS, response));
    }

    @Override
    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<SuccessResponse> completePayment(
            @AuthenticationPrincipal String userId,
            @PathVariable String paymentId,
            @RequestBody @Valid CompletePaymentRequest request) {
        PaymentResponse response = paymentService.completePayment(userId, paymentId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_COMPLETE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse> cancelPayment(
            @AuthenticationPrincipal String userId,
            @PathVariable String paymentId) {
        paymentService.cancelPayment(userId, paymentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_CANCEL_SUCCESS));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMyPayments(
            @AuthenticationPrincipal String userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PaymentResponse> response = paymentService.getMyPayments(userId, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse> getPayment(
            @AuthenticationPrincipal String userId,
            @PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPayment(userId, paymentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PAYMENT_GET_SUCCESS, response));
    }
}
