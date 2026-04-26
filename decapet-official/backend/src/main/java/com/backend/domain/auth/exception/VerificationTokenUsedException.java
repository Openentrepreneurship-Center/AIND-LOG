package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class VerificationTokenUsedException extends BusinessException {

    public VerificationTokenUsedException() {
        super(ErrorCode.VERIFICATION_TOKEN_USED);
    }
}
