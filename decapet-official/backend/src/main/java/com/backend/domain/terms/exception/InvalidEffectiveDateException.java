package com.backend.domain.terms.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidEffectiveDateException extends BusinessException {

    public InvalidEffectiveDateException() {
        super(ErrorCode.INVALID_EFFECTIVE_DATE);
    }
}
