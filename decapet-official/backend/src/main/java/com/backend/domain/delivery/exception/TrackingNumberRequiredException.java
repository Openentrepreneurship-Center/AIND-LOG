package com.backend.domain.delivery.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class TrackingNumberRequiredException extends BusinessException {

	public TrackingNumberRequiredException() {
		super(ErrorCode.TRACKING_NUMBER_REQUIRED);
	}
}
