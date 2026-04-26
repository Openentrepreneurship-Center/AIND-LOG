package com.backend.domain.user.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.user.dto.response.UserResponse;
import com.backend.domain.user.entity.User;

@Component
public class UserResponseMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getName(),
                user.getZipCode(),
                user.getAddress(),
                user.getDetailAddress(),
                user.getRecipientName(),
                user.getRecipientPhone(),
                user.getPermissions()
        );
    }
}
