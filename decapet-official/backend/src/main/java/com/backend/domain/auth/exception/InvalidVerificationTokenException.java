package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidVerificationTokenException extends BusinessException {

	public InvalidVerificationTokenException() {
		super(ErrorCode.VERIFICATION_TOKEN_NOT_FOUND);
	}
}
