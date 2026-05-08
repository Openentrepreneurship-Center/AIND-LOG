package com.backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.user.dto.request.ChangePasswordRequest;
import com.backend.domain.user.dto.request.PhoneChangeRequest;
import com.backend.domain.user.dto.request.PhoneVerifyRequest;
import com.backend.domain.user.dto.request.UpdateProfileRequest;
import com.backend.domain.user.dto.response.UserResponse;
import com.backend.domain.user.service.UserService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;
import com.backend.global.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final CookieUtil cookieUtil;

    @Override
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getMe(@AuthenticationPrincipal String userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
    }

    @Override
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse> deleteAccount(@AuthenticationPrincipal String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.USER_DELETE_SUCCESS));
    }

    @Override
    @PostMapping("/me/phone/sms/send")
    public ResponseEntity<SuccessResponse> sendPhoneChangeSms(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid PhoneChangeRequest request) {
        userService.sendPhoneChangeSms(userId, request.phone());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_PHONE_SMS_SEND_SUCCESS));
    }

    @Override
    @PostMapping("/me/phone/verify")
    public ResponseEntity<SuccessResponse> verifyPhoneChange(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid PhoneVerifyRequest request) {
        userService.verifyPhoneChange(userId, request.phone(), request.code());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_PHONE_CHANGE_SUCCESS));
    }

    @Override
    @PostMapping("/me/password")
    public ResponseEntity<SuccessResponse> changePassword(
            @AuthenticationPrincipal String userId,
            HttpServletRequest httpRequest,
            @RequestBody @Valid ChangePasswordRequest request) {
        String accessToken = cookieUtil.extractAccessToken(httpRequest);
        userService.changePassword(userId, accessToken, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.USER_PASSWORD_CHANGE_SUCCESS));
    }
}
