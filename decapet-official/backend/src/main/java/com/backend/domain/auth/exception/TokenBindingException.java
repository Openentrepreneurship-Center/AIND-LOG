package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

/**
 * Thrown when token binding validation fails.
 * This indicates the token is being used from a different
 * device/browser than it was originally issued to.
 */
public class TokenBindingException extends BusinessException {

    public TokenBindingException() {
        super(ErrorCode.TOKEN_BINDING_MISMATCH);
    }
}
