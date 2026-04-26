package com.backend.global.error.exception;

import com.backend.global.error.ErrorCode;

public class ValidationFailedException extends BusinessException {
    public ValidationFailedException() {
        super(ErrorCode.VALIDATION_FAILED);
    }

    public ValidationFailedException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }
}
