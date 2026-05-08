package com.backend.domain.terms.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class TermNotFoundException extends BusinessException {

    public TermNotFoundException() {
        super(ErrorCode.TERM_NOT_FOUND);
    }
}
