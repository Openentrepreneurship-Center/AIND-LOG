package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InformationSharingNotAllowedException extends BusinessException {
    public InformationSharingNotAllowedException() {
        super(ErrorCode.INFORMATION_SHARING_NOT_ALLOWED);
    }
}
