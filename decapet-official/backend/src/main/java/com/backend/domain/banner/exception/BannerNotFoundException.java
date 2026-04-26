package com.backend.domain.banner.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class BannerNotFoundException extends BusinessException {

    public BannerNotFoundException() {
        super(ErrorCode.BANNER_NOT_FOUND);
    }
}
