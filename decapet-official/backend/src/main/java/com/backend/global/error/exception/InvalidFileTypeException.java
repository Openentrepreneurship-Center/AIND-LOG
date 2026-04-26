package com.backend.global.error.exception;

import com.backend.global.error.ErrorCode;

public class InvalidFileTypeException extends BusinessException {
    public InvalidFileTypeException() {
        super(ErrorCode.INVALID_FILE_TYPE);
    }
}
