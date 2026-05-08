package com.backend.domain.terms.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateTermVersionException extends BusinessException {

    public DuplicateTermVersionException() {
        super(ErrorCode.DUPLICATE_TERM_VERSION);
    }
}
