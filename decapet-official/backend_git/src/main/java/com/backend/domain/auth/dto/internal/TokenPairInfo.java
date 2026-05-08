package com.backend.domain.auth.dto.internal;

import java.util.Date;

public record TokenPairInfo(
        String accessToken,
        String accessTokenJti,
        String refreshToken,
        Date accessTokenExpiresAt
) {
}
