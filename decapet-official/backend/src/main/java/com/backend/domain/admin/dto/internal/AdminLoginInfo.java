package com.backend.domain.admin.dto.internal;

import com.backend.domain.admin.dto.response.AdminLoginResponse;

public record AdminLoginInfo(
        String tempToken,
        AdminLoginResponse response
) {
}
