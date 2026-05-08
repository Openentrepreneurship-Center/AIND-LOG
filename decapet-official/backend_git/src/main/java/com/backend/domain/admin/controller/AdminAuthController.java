package com.backend.domain.admin.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.admin.dto.request.AdminChangePasswordRequest;
import com.backend.domain.admin.dto.request.AdminLoginRequest;
import com.backend.domain.admin.dto.request.AdminOtpVerifyRequest;
import com.backend.domain.admin.dto.response.AdminLoginResponse;
import com.backend.domain.admin.dto.response.OtpSetupResponse;
import com.backend.domain.admin.service.AdminAuthService;
import com.backend.domain.admin.service.AdminAuthService.AdminLoginResult;
import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;
import com.backend.global.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController implements AdminAuthApi {

    private final AdminAuthService adminAuthService;
    private final CookieUtil cookieUtil;

    @Override
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(
            @RequestBody @Valid AdminLoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request);

        AdminLoginResponse loginResponse = new AdminLoginResponse(result.status());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieUtil.createTempTokenCookie(result.tempToken()).toString())
                .body(SuccessResponse.of(SuccessCode.ADMIN_LOGIN_SUCCESS, loginResponse));
    }

    @Override
    @PostMapping("/otp/setup")
    public ResponseEntity<SuccessResponse> setupOtp(HttpServletRequest request) {
        String tempToken = cookieUtil.extractTempToken(request);
        OtpSetupResponse response = adminAuthService.setupOtp(tempToken);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_OTP_SETUP_SUCCESS, response));
    }

    @Override
    @PostMapping("/otp/verify")
    public ResponseEntity<SuccessResponse> verifyOtp(
            HttpServletRequest request,
            @RequestBody @Valid AdminOtpVerifyRequest otpRequest) {
        String tempToken = cookieUtil.extractTempToken(request);
        TokenPairInfo tokenPair = adminAuthService.verifyOtp(tempToken, otpRequest.otpCode());

        return ResponseEntity.ok()
                .headers(cookieUtil.createAdminTokenHeaders(tokenPair))
                .header(HttpHeaders.SET_COOKIE, cookieUtil.createTempTokenClearCookie().toString())
                .body(SuccessResponse.of(SuccessCode.ADMIN_LOGIN_SUCCESS));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse> refresh(HttpServletRequest request) {
        String refreshToken = cookieUtil.extractAdminRefreshToken(request);
        TokenPairInfo tokenPair = adminAuthService.refresh(refreshToken);

        return ResponseEntity.ok()
                .headers(cookieUtil.createAdminTokenHeaders(tokenPair))
                .body(SuccessResponse.of(SuccessCode.ADMIN_TOKEN_REFRESH_SUCCESS));
    }

    @Override
    @PutMapping("/password")
    public ResponseEntity<SuccessResponse> changePassword(
            HttpServletRequest request,
            @AuthenticationPrincipal String adminId,
            @RequestBody @Valid AdminChangePasswordRequest passwordRequest) {
        String accessToken = cookieUtil.extractAdminAccessToken(request);
        adminAuthService.changePassword(adminId, accessToken, passwordRequest.currentPassword(), passwordRequest.newPassword());

        return ResponseEntity.ok()
                .headers(cookieUtil.createAdminTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.ADMIN_PASSWORD_CHANGE_SUCCESS));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
        String accessToken = cookieUtil.extractAdminAccessToken(request);
        String refreshToken = cookieUtil.extractAdminRefreshToken(request);
        adminAuthService.logout(accessToken, refreshToken);

        return ResponseEntity.ok()
                .headers(cookieUtil.createAdminTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.ADMIN_LOGOUT_SUCCESS));
    }
}
