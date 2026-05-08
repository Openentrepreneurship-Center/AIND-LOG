package com.backend.domain.product.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ImageRequiredException extends BusinessException {

    public ImageRequiredException() {
        super(ErrorCode.IMAGE_REQUIRED);
    }
}
