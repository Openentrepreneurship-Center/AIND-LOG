package com.backend.domain.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.backend.domain.payment.dto.response.TossPaymentsConfirmResponse;
import com.backend.domain.payment.exception.PaymentCancelFailedException;
import com.backend.domain.payment.exception.PaymentConfirmFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentsService {

    private final RestClient tossPaymentsRestClient;

    public TossPaymentsConfirmResponse confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount.longValue()
        );

        try {
            TossPaymentsConfirmResponse response = tossPaymentsRestClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", paymentKey)
                    .body(requestBody)
                    .retrieve()
                    .body(TossPaymentsConfirmResponse.class);

            validateConfirmResponse(response, orderId, amount);

            return response;
        } catch (RestClientResponseException e) {
            log.error("토스페이먼츠 결제 승인 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentConfirmFailedException(e.getResponseBodyAsString());
        }
    }

    public void cancelPayment(String paymentKey, String cancelReason) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        try {
            tossPaymentsRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("토스페이먼츠 결제 취소 실패: paymentKey={}, status={}, body={}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentCancelFailedException(paymentKey, e.getStatusCode().value());
        }
    }

    private void validateConfirmResponse(TossPaymentsConfirmResponse response, String expectedOrderId, BigDecimal expectedAmount) {
        if (response == null) {
            throw new PaymentConfirmFailedException("토스페이먼츠 응답이 null입니다.");
        }

        if (!"DONE".equals(response.status())) {
            log.error("토스페이먼츠 결제 상태 불일치: expected=DONE, actual={}", response.status());
            throw new PaymentConfirmFailedException("결제 상태가 DONE이 아닙니다: " + response.status());
        }

        if (!expectedOrderId.equals(response.orderId())) {
            log.error("토스페이먼츠 주문번호 불일치: expected={}, actual={}", expectedOrderId, response.orderId());
            throw new PaymentConfirmFailedException("주문번호가 일치하지 않습니다.");
        }

        if (expectedAmount.longValue() != response.totalAmount()) {
            log.error("토스페이먼츠 결제 금액 불일치: expected={}, actual={}", expectedAmount.longValue(), response.totalAmount());
            throw new PaymentConfirmFailedException("결제 금액이 일치하지 않습니다.");
        }
    }
}
