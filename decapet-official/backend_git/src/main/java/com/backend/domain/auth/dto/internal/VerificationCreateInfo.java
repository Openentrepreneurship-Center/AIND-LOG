package com.backend.domain.auth.dto.internal;

public record VerificationCreateInfo(
        String phone,
        String code,
        int ttlMinutes
) {
}
