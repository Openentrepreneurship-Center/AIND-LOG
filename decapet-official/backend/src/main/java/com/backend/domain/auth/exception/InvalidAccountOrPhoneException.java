package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidAccountOrPhoneException extends BusinessException {

    public InvalidAccountOrPhoneException() {
        super(ErrorCode.INVALID_ACCOUNT_OR_PHONE);
    }
}
