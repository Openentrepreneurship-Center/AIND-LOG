package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidTempTokenException extends BusinessException {

    public InvalidTempTokenException() {
        super(ErrorCode.INVALID_TEMP_TOKEN);
    }
}
