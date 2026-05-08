package com.backend.domain.user.dto.response;

import java.util.Set;

import com.backend.domain.user.entity.PermissionType;

public record UserResponse(
	String id,
	String email,
	String phone,
	String name,
	String zipCode,
	String address,
	String detailAddress,
	String recipientName,
	String recipientPhone,
	Set<PermissionType> permissions
) {
}
