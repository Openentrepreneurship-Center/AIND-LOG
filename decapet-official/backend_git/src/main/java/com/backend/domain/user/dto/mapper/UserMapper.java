package com.backend.domain.user.dto.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.backend.domain.auth.dto.request.RegisterRequest;
import com.backend.domain.user.dto.internal.ProfileUpdateInfo;
import com.backend.domain.user.dto.request.UpdateProfileRequest;
import com.backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .name(request.name())
                .zipCode(request.zipCode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .build();
    }

    public ProfileUpdateInfo toProfileUpdateInfo(UpdateProfileRequest request) {
        return new ProfileUpdateInfo(
                request.name(),
                null,
                request.zipCode(),
                request.address(),
                request.detailAddress(),
                request.recipientName(),
                request.recipientPhone()
        );
    }
}
