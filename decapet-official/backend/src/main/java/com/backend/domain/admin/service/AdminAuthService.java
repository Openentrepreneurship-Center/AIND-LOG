package com.backend.domain.admin.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.admin.dto.request.AdminLoginRequest;
import com.backend.domain.admin.dto.response.AdminLoginResponse;
import com.backend.domain.admin.dto.response.OtpSetupResponse;
import com.backend.domain.admin.entity.Admin;
import com.backend.domain.admin.entity.AdminLoginStatus;
import com.backend.domain.admin.exception.AdminAccountLockedException;
import com.backend.domain.admin.exception.InvalidAdminPasswordException;
import com.backend.domain.admin.exception.InvalidOtpException;
import com.backend.domain.admin.repository.AdminRepository;
import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.domain.auth.exception.InvalidTempTokenException;
import com.backend.domain.auth.service.TokenService;
import com.backend.global.security.TotpProvider;
import com.backend.global.util.AesEncryptor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String TEMP_TOKEN_PURPOSE_OTP = "OTP_VERIFICATION";
    private static final String TEMP_TOKEN_PURPOSE_SETUP = "OTP_SETUP";
    private static final Set<String> VALID_OTP_PURPOSES = Set.of(TEMP_TOKEN_PURPOSE_OTP, TEMP_TOKEN_PURPOSE_SETUP);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TotpProvider totpProvider;
    private final AesEncryptor aesEncryptor;

    @Transactional
    public AdminLoginResult login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByLoginIdOrThrow(request.loginId());

        if (admin.isLocked()) {
            throw new AdminAccountLockedException();
        }

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            admin.recordLoginFailure();
            throw new InvalidAdminPasswordException();
        }

        admin.resetLoginAttempts();

        if (admin.isOtpEnabled()) {
            String tempToken = tokenService.createTempToken(admin.getId(), TEMP_TOKEN_PURPOSE_OTP);
            return AdminLoginResult.otpRequired(tempToken);
        } else {
            String tempToken = tokenService.createTempToken(admin.getId(), TEMP_TOKEN_PURPOSE_SETUP);
            return AdminLoginResult.otpSetupRequired(tempToken);
        }
    }

    public record AdminLoginResult(
            AdminLoginStatus status,
            String tempToken,
            TokenPairInfo tokenPair
    ) {
        static AdminLoginResult otpRequired(String tempToken) {
            return new AdminLoginResult(AdminLoginStatus.OTP_REQUIRED, tempToken, null);
        }

        static AdminLoginResult otpSetupRequired(String tempToken) {
            return new AdminLoginResult(AdminLoginStatus.OTP_SETUP_REQUIRED, tempToken, null);
        }
    }

    @Transactional
    public TokenPairInfo verifyOtp(String tempToken, String otpCode) {
        String purpose = tokenService.validateTempTokenAndGetPurpose(tempToken);
        if (!VALID_OTP_PURPOSES.contains(purpose)) {
            throw new InvalidTempTokenException();
        }

        String adminId = tokenService.getUserIdFromTempToken(tempToken);
        Admin admin = adminRepository.findByIdOrThrow(adminId);

        String decryptedSecret = aesEncryptor.decrypt(admin.getOtpSecret());
        if (!totpProvider.verifyCode(decryptedSecret, otpCode)) {
            throw new InvalidOtpException();
        }

        if (!admin.isOtpEnabled()) {
            admin.enableOtp();
            adminRepository.save(admin);
        }

        tokenService.invalidateAllTokensByUserId(admin.getId());

        return tokenService.createTokenPair(admin.getId(), "ADMIN", List.of());
    }

    @Transactional
    public OtpSetupResponse setupOtp(String tempToken) {
        String purpose = tokenService.validateTempTokenAndGetPurpose(tempToken);
        if (!TEMP_TOKEN_PURPOSE_SETUP.equals(purpose)) {
            throw new InvalidTempTokenException();
        }

        String adminId = tokenService.getUserIdFromTempToken(tempToken);
        Admin admin = adminRepository.findByIdOrThrow(adminId);

        String secret = totpProvider.generateSecret();
        String qrCodeUri = totpProvider.generateQrCodeUri(secret, admin.getLoginId(), "Decapet");

        admin.setupOtp(aesEncryptor.encrypt(secret));
        adminRepository.save(admin);

        return new OtpSetupResponse(secret, qrCodeUri);
    }

    @Transactional
    public void changePassword(String adminId, String accessToken, String currentPassword, String newPassword) {
        Admin admin = adminRepository.findByIdOrThrow(adminId);

        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new InvalidAdminPasswordException();
        }

        admin.updatePassword(passwordEncoder.encode(newPassword));

        tokenService.blacklistAccessToken(accessToken);
        tokenService.invalidateAllTokensByUserId(adminId);
    }

    @Transactional
    public TokenPairInfo refresh(String refreshToken) {
        return tokenService.refreshTokens(refreshToken, "ADMIN");
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenService.blacklistAccessToken(accessToken);
        if (refreshToken != null) {
            tokenService.invalidateRefreshToken(refreshToken);
        }
    }

}
