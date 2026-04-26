package com.backend.domain.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssignTrackingRequest(
        @NotBlank(message = "운송장 번호를 입력해주세요.")
        String trackingNumber
) {
}
