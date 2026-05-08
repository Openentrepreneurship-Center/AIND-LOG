package com.backend.domain.terms.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InactiveTermException extends BusinessException {

    public InactiveTermException() {
        super(ErrorCode.INACTIVE_TERM);
    }
}
