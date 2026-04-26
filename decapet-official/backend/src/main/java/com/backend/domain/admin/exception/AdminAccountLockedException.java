package com.backend.domain.admin.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AdminAccountLockedException extends BusinessException {
    public AdminAccountLockedException() {
        super(ErrorCode.ADMIN_ACCOUNT_LOCKED);
    }
}
