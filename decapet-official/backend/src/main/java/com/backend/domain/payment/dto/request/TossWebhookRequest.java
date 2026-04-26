package com.backend.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossWebhookRequest(
        String eventType,
        String createdAt,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String paymentKey,
            String orderId,
            String status
    ) {
    }
}
