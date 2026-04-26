package com.backend.domain.user.dto.request;

import java.util.Set;

import com.backend.domain.user.entity.PermissionType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BulkUpdatePermissionsRequest(
    @NotEmpty(message = "사용자 ID 목록은 필수입니다.")
    Set<String> userIds,

    @NotNull(message = "권한 목록은 필수입니다.")
    Set<PermissionType> permissions
) {}
