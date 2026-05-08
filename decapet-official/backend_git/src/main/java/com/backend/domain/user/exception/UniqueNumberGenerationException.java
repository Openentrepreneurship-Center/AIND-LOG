package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class UniqueNumberGenerationException extends BusinessException {

	public UniqueNumberGenerationException() {
		super(ErrorCode.UNIQUE_NUMBER_GENERATION_FAILED);
	}
}
