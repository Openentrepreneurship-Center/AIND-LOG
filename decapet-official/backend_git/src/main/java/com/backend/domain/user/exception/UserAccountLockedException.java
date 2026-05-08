package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class UserAccountLockedException extends BusinessException {
	public UserAccountLockedException() {
		super(ErrorCode.USER_ACCOUNT_LOCKED);
	}
}
