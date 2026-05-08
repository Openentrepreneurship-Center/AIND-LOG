package com.backend.domain.admin.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidOtpException extends BusinessException {

    public InvalidOtpException() {
        super(ErrorCode.INVALID_OTP);
    }

    public InvalidOtpException(String message) {
        super(ErrorCode.INVALID_OTP, message);
    }
}
