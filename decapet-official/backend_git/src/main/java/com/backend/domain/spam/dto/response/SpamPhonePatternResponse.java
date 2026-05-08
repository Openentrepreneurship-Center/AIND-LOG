package com.backend.domain.spam.dto.response;

import java.time.LocalDateTime;

public record SpamPhonePatternResponse(
    Long id,
    String pattern,
    LocalDateTime createdAt
) {
}
