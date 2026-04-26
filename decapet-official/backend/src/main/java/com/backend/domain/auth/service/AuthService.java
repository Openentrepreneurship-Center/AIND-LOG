package com.backend.domain.auth.service;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.domain.auth.dto.internal.VerificationCreateInfo;
import com.backend.domain.auth.dto.mapper.VerificationMapper;
import com.backend.domain.auth.dto.mapper.VerificationTokenMapper;
import com.backend.domain.auth.dto.request.LoginRequest;
import com.backend.domain.auth.dto.request.PasswordResetRequest;
import com.backend.domain.auth.dto.request.PasswordSmsSendRequest;
import com.backend.domain.auth.dto.request.RegisterRequest;
import com.backend.domain.auth.dto.request.SmsSendRequest;
import com.backend.domain.auth.dto.request.SmsVerifyRequest;
import com.backend.domain.auth.dto.request.TermConsentInfo;
import com.backend.domain.auth.dto.response.EmailCheckResponse;
import com.backend.domain.auth.dto.response.VerificationTokenResponse;
import com.backend.domain.auth.entity.Verification;
import com.backend.domain.auth.repository.VerificationRepository;
import com.backend.domain.terms.dto.mapper.UserTermConsentMapper;
import com.backend.domain.terms.entity.Term;
import com.backend.domain.terms.service.TermService;
import com.backend.domain.user.dto.mapper.UserMapper;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.exception.InvalidPasswordException;
import com.backend.domain.user.exception.UserAccountLockedException;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int VERIFICATION_TTL_MINUTES = 5;

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final TermService termService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final SmsService smsService;
    private final VerificationMapper verificationMapper;
	private final VerificationTokenMapper verificationTokenMapper;
    private final UserMapper userMapper;
    private final UserTermConsentMapper userTermConsentMapper;
    private final TransactionTemplate transactionTemplate;

    private final SecureRandom random = new SecureRandom();

    public void sendSms(SmsSendRequest request) {
        userRepository.validatePhoneNotDuplicate(request.phone());

        String code = transactionTemplate.execute(status -> {
            verificationRepository.deleteByPhone(request.phone());
            String verificationCode = generateVerificationCode();
            Verification verification = verificationMapper.toEntity(
                    new VerificationCreateInfo(request.phone(), verificationCode, VERIFICATION_TTL_MINUTES)
            );
            verificationRepository.save(verification);
            return verificationCode;
        });

        smsService.sendVerificationCode(request.phone(), code);
    }

    @Transactional
    public VerificationTokenResponse verifySms(SmsVerifyRequest request) {
        Verification verification = verificationRepository
                .getByPhoneAndCodeAndVerifiedAtIsNull(request.phone(), request.code());

        return verificationTokenMapper.toResponse(verification);
    }

    @Transactional
    public TokenPairInfo register(RegisterRequest request) {
        Verification verification = verificationRepository
                .getByToken(request.token(), request.phone());

        userRepository.validateEmailNotDuplicate(request.email());
        userRepository.validatePhoneNotDuplicate(request.phone());

        List<String> agreedTermIds = request.termConsents().stream()
                .map(TermConsentInfo::termId)
                .toList();
        termService.validateRequiredTermsAgreed(agreedTermIds);

        User user = userMapper.toEntity(request);
        userRepository.save(user);

        for (TermConsentInfo consent : request.termConsents()) {
            Term term = termService.getActiveTerm(consent.termId());
            user.addTermConsent(userTermConsentMapper.toEntity(term));
        }

        verification.markTokenUsed();

        return tokenService.createTokenPair(user.getId(), "USER", toPermissionStrings(user.getPermissions()));
    }

    @Transactional
    public TokenPairInfo login(LoginRequest request) {
		User user = userRepository.getByEmail(request.email());

        if (user.isLocked()) {
            throw new UserAccountLockedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.recordLoginFailure();
            throw new InvalidPasswordException();
        }

        user.resetLoginAttempts();
        tokenService.invalidateAllTokensByUserId(user.getId());

        return tokenService.createTokenPair(user.getId(), "USER", toPermissionStrings(user.getPermissions()));
    }

    @Transactional
    public TokenPairInfo refresh(String refreshToken) {
        return tokenService.refreshTokens(refreshToken, "USER");
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenService.blacklistAccessToken(accessToken);
        if (refreshToken != null) {
            tokenService.invalidateRefreshToken(refreshToken);
        }
    }

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmailAvailable(String email) {
        boolean exists = userRepository.existsByEmailAndDeletedAtIsNull(email);
        return new EmailCheckResponse(!exists);
    }

    public void sendPasswordResetSms(PasswordSmsSendRequest request) {
        userRepository.getByEmailAndPhone(request.email(), request.phone());

        String code = transactionTemplate.execute(status -> {
            verificationRepository.deleteByPhone(request.phone());
            String verificationCode = generateVerificationCode();
            Verification verification = verificationMapper.toEntity(
                    new VerificationCreateInfo(request.phone(), verificationCode, VERIFICATION_TTL_MINUTES)
            );
            verificationRepository.save(verification);
            return verificationCode;
        });

        smsService.sendVerificationCode(request.phone(), code);
    }

    @Transactional
    public VerificationTokenResponse verifyPasswordResetSms(SmsVerifyRequest request) {
        return verifySms(request);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        Verification verification = verificationRepository.getByToken(request.token(), request.phone());
        User user = userRepository.getByPhone(request.phone());

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        verification.markTokenUsed();

        tokenService.invalidateAllTokensByUserId(user.getId());
    }

    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private List<String> toPermissionStrings(Collection<PermissionType> permissions) {
        return permissions.stream()
                .map(PermissionType::name)
                .toList();
    }
}
