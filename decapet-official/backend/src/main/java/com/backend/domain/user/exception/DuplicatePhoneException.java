package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicatePhoneException extends BusinessException {

    public DuplicatePhoneException() {
        super(ErrorCode.DUPLICATE_PHONE);
    }
}
