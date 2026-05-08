package com.backend.domain.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.payment.dto.request.TossWebhookRequest;
import com.backend.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/webhooks/tosspayments")
@RequiredArgsConstructor
@Slf4j
public class TossWebhookController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody TossWebhookRequest request) {
        log.info("토스페이먼츠 웹훅 수신: eventType={}, paymentKey={}",
                request.eventType(),
                request.data() != null ? request.data().paymentKey() : "null");

        if ("PAYMENT_STATUS_CHANGED".equals(request.eventType()) && request.data() != null) {
            paymentService.handleWebhookPaymentStatusChanged(
                    request.data().paymentKey(),
                    request.data().status()
            );
        }

        return ResponseEntity.ok().build();
    }
}
