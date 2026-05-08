package com.backend.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.auth.entity.Verification;
import com.backend.domain.auth.exception.InvalidVerificationException;
import com.backend.domain.auth.exception.InvalidVerificationTokenException;
import com.backend.domain.auth.exception.VerificationCodeExpiredException;
import com.backend.domain.auth.exception.VerificationTokenUsedException;

public interface VerificationRepository extends JpaRepository<Verification, String> {

    Optional<Verification> findByPhoneAndCodeAndVerifiedAtIsNull(String phone, String code);

	default Verification getByPhoneAndCodeAndVerifiedAtIsNull(String phone, String code) {
		Verification verification = findByPhoneAndCodeAndVerifiedAtIsNull(phone, code)
			.orElseThrow(InvalidVerificationException::new);

		if (verification.isExpired()) {
			throw new VerificationCodeExpiredException();
		}
		verification.verify();
		return verification;
	}

    Optional<Verification> findByToken(String token);

	default Verification getByToken(String token, String phone) {
		Verification verification = findByToken(token)
			.orElseThrow(InvalidVerificationTokenException::new);

		if (verification.isExpired()) {
			throw new VerificationCodeExpiredException();
		}

		if (verification.isTokenUsed()) {
			throw new VerificationTokenUsedException();
		}

		if (!verification.getPhone().equals(phone)) {
			throw new InvalidVerificationException();
		}

		return verification;
	}


    void deleteByPhone(String phone);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
