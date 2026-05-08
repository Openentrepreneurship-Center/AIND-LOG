package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

/**
 * Thrown when a refresh token reuse attack is detected.
 * This indicates a potential token theft - the same refresh token
 * was used multiple times, suggesting it was stolen.
 */
public class TokenReuseException extends BusinessException {

    public TokenReuseException() {
        super(ErrorCode.TOKEN_REUSE_DETECTED);
    }
}
