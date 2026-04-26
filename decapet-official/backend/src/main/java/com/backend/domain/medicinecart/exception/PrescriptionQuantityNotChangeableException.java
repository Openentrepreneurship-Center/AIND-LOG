package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionQuantityNotChangeableException extends BusinessException {

	public PrescriptionQuantityNotChangeableException() {
		super(ErrorCode.PRESCRIPTION_QUANTITY_NOT_CHANGEABLE);
	}
}
