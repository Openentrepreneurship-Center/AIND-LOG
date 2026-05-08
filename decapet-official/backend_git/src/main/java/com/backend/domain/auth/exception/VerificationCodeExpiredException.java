package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class VerificationCodeExpiredException extends BusinessException {

	public VerificationCodeExpiredException() {
		super(ErrorCode.VERIFICATION_CODE_EXPIRED);
	}
}
