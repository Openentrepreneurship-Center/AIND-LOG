package com.backend.domain.pet.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidBirthdateException extends BusinessException {
    public InvalidBirthdateException() {
        super(ErrorCode.INVALID_BIRTHDATE);
    }
}
