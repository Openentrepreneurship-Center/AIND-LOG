package com.backend.domain.admin.dto.response;

public record OtpSetupResponse(
        String secret,
        String qrCodeUri
) {
}
