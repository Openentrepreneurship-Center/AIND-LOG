package com.backend.domain.admin.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidAdminPasswordException extends BusinessException {

    public InvalidAdminPasswordException() {
        super(ErrorCode.INVALID_ADMIN_PASSWORD);
    }
}
