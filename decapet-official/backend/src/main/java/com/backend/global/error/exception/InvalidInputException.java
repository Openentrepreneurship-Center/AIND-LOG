package com.backend.global.error.exception;

import com.backend.global.error.ErrorCode;

public class InvalidInputException extends BusinessException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT);
    }
}
