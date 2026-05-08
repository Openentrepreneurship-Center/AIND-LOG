package com.backend.global.error;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private final HttpStatus httpStatus;
	private final String code;
	private final String errorMessage;
	private final Map<String, String> fieldErrors;

	public ErrorResponse(ErrorCode code) {
		this.httpStatus = code.getStatus();
		this.code = code.getCode();
		this.errorMessage = code.getMessage();
		this.fieldErrors = null;
	}

	public ErrorResponse(ErrorCode code, Map<String, String> fieldErrors) {
		this.httpStatus = code.getStatus();
		this.code = code.getCode();
		this.errorMessage = code.getMessage();
		this.fieldErrors = fieldErrors;
	}

	public ErrorResponse(HttpStatus httpStatus, String code, String errorMessage) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.errorMessage = errorMessage;
		this.fieldErrors = null;
	}

	public ErrorResponse(String code, String errorMessage) {
		this.httpStatus = null;
		this.code = code;
		this.errorMessage = errorMessage;
		this.fieldErrors = null;
	}
}
