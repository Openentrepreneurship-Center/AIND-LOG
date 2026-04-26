package com.backend.domain.terms.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class RequiredTermNotAgreedException extends BusinessException {

    public RequiredTermNotAgreedException() {
        super(ErrorCode.REQUIRED_TERM_NOT_AGREED);
    }
}
