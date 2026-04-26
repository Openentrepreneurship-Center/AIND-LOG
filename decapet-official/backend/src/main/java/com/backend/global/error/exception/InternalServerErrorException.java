package com.backend.global.error.exception;

import com.backend.global.error.ErrorCode;

public class InternalServerErrorException extends BusinessException {
    public InternalServerErrorException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
