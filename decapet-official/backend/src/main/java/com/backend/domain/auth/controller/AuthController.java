package com.backend.domain.auth.controller;

import static com.backend.global.common.SuccessCode.SMS_SEND_SUCCESS;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.domain.auth.dto.request.LoginRequest;
import com.backend.domain.auth.dto.request.PasswordResetRequest;
import com.backend.domain.auth.dto.request.PasswordSmsSendRequest;
import com.backend.domain.auth.dto.request.RegisterRequest;
import com.backend.domain.auth.dto.request.SmsSendRequest;
import com.backend.domain.auth.dto.request.SmsVerifyRequest;
import com.backend.domain.auth.dto.response.EmailCheckResponse;
import com.backend.domain.auth.dto.response.VerificationTokenResponse;
import com.backend.domain.auth.service.AuthService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;
import com.backend.global.util.CookieUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Override
    @PostMapping("/sms/send")
    public ResponseEntity<SuccessResponse> sendSms(@RequestBody @Valid SmsSendRequest request) {
        authService.sendSms(request);
        return ResponseEntity.ok(SuccessResponse.of(SMS_SEND_SUCCESS));
    }

    @Override
    @PostMapping("/sms/verify")
    public ResponseEntity<SuccessResponse> verifySms(@RequestBody @Valid SmsVerifyRequest request) {
        VerificationTokenResponse response = authService.verifySms(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SMS_VERIFY_SUCCESS, response));
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) {
        TokenPairInfo tokenPair = authService.register(request);

        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenHeaders(tokenPair))
                .body(SuccessResponse.of(SuccessCode.REGISTER_SUCCESS));
    }

    @Override
    @GetMapping("/check-email")
    public ResponseEntity<SuccessResponse> checkEmailAvailable(@RequestParam String email) {
        EmailCheckResponse response = authService.checkEmailAvailable(email);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.EMAIL_CHECK_SUCCESS, response));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenPairInfo tokenPair = authService.login(request);

        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenHeaders(tokenPair))
                .body(SuccessResponse.of(SuccessCode.LOGIN_SUCCESS));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse> refresh(HttpServletRequest request) {
        String refreshToken = cookieUtil.extractRefreshToken(request);
        TokenPairInfo tokenPair = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenHeaders(tokenPair))
                .body(SuccessResponse.of(SuccessCode.TOKEN_REFRESH_SUCCESS));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
        String accessToken = cookieUtil.extractAccessToken(request);
        String refreshToken = cookieUtil.extractRefreshToken(request);
        authService.logout(accessToken, refreshToken);

        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.LOGOUT_SUCCESS));
    }

    @Override
    @PostMapping("/password/sms/send")
    public ResponseEntity<SuccessResponse> sendPasswordResetSms(@RequestBody @Valid PasswordSmsSendRequest request) {
        authService.sendPasswordResetSms(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PASSWORD_SMS_SEND_SUCCESS));
    }

    @Override
    @PostMapping("/password/sms/verify")
    public ResponseEntity<SuccessResponse> verifyPasswordResetSms(@RequestBody @Valid SmsVerifyRequest request) {
        VerificationTokenResponse response = authService.verifyPasswordResetSms(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PASSWORD_SMS_VERIFY_SUCCESS, response));
    }

    @Override
    @PostMapping("/password/reset")
    public ResponseEntity<SuccessResponse> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PASSWORD_RESET_SUCCESS));
    }
}
