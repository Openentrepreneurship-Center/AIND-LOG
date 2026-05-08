package com.backend.domain.auth.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.auth.dto.response.VerificationTokenResponse;
import com.backend.domain.auth.entity.Verification;

@Component
public class VerificationTokenMapper {
	public VerificationTokenResponse toResponse(Verification verification) {
		return new VerificationTokenResponse(verification.getToken());
	}
}
