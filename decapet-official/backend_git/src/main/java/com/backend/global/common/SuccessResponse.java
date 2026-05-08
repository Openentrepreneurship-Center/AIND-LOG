package com.backend.global.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SuccessResponse {
	private final String code;
	private final HttpStatus httpStatus;
	private final String message;
	private final Object data;

	public static SuccessResponse of(SuccessCode successCode){
		return new SuccessResponse(successCode, null);
	}
	public static SuccessResponse of(SuccessCode successCode, Object data) {
		return new SuccessResponse(successCode, data);
	}

	public SuccessResponse(SuccessCode code, Object data) {
		this.code = code.getCode();
		this.httpStatus = code.getStatus();
		this.message = code.getMessage();
		this.data = data;
	}
}