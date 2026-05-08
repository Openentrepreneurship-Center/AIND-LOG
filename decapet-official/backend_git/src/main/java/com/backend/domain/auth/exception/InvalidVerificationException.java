package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidVerificationException extends BusinessException {

    public InvalidVerificationException() {
        super(ErrorCode.INVALID_VERIFICATION_CODE);
    }
}
