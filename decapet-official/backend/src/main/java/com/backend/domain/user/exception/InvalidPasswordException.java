package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidPasswordException extends BusinessException {

	public InvalidPasswordException() {
		super(ErrorCode.INVALID_PASSWORD);
	}
}
