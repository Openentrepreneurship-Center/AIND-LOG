package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ProductPurchaseNotAllowedException extends BusinessException {
    public ProductPurchaseNotAllowedException() {
        super(ErrorCode.PRODUCT_PURCHASE_NOT_ALLOWED);
    }
}
