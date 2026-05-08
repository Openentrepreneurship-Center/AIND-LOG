package com.backend.domain.user.service;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.domain.auth.dto.internal.VerificationCreateInfo;
import com.backend.domain.auth.dto.mapper.VerificationMapper;
import com.backend.domain.auth.entity.Verification;
import com.backend.domain.auth.repository.RefreshTokenRepository;
import com.backend.domain.auth.repository.VerificationRepository;
import com.backend.domain.auth.service.SmsService;
import com.backend.domain.auth.service.TokenService;
import com.backend.domain.user.dto.mapper.UserMapper;
import com.backend.domain.user.dto.mapper.UserResponseMapper;
import com.backend.domain.user.dto.request.UpdateProfileRequest;
import com.backend.domain.user.dto.response.UserResponse;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.event.UserDeletedEvent;
import com.backend.domain.user.exception.InvalidPasswordException;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int VERIFICATION_TTL_MINUTES = 5;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserResponseMapper userResponseMapper;
    private final VerificationRepository verificationRepository;
    private final VerificationMapper verificationMapper;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        return userResponseMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        // phone 변경은 SMS 인증 플로우(sendPhoneChangeSms → verifyPhoneChange)를 통해서만 허용
        user.updateProfile(userMapper.toProfileUpdateInfo(request));

        return userResponseMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        // 즉시 처리: 토큰 무효화 + PII 익명화 + soft delete
        refreshTokenRepository.deleteByUserId(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.delete();

        // 비동기 cascade 삭제 (트랜잭션 커밋 후 실행)
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }

    public void sendPhoneChangeSms(String userId, String phone) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        userRepository.validatePhoneNotDuplicate(phone, user.getPhone());

        String code = transactionTemplate.execute(status -> {
            verificationRepository.deleteByPhone(phone);
            String verificationCode = generateVerificationCode();
            Verification verification = verificationMapper.toEntity(
                    new VerificationCreateInfo(phone, verificationCode, VERIFICATION_TTL_MINUTES)
            );
            verificationRepository.save(verification);
            return verificationCode;
        });

        smsService.sendVerificationCode(phone, code);
    }

    @Transactional
    public void verifyPhoneChange(String userId, String phone, String code) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        verificationRepository.getByPhoneAndCodeAndVerifiedAtIsNull(phone, code);

        user.updateProfile(new com.backend.domain.user.dto.internal.ProfileUpdateInfo(
                null, phone, null, null, null, null, null
        ));
    }

    @Transactional
    public void changePassword(String userId, String accessToken, String currentPassword, String newPassword) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.updatePassword(passwordEncoder.encode(newPassword));

        tokenService.blacklistAccessToken(accessToken);
        tokenService.invalidateAllTokensByUserId(userId);
    }

    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
