package com.backend.domain.user.dto.request;

import java.util.Set;

import com.backend.domain.user.entity.PermissionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "회원 권한 수정 요청")
public record UpdatePermissionsRequest(
		@Schema(description = "권한 목록", example = "[\"PRODUCT_PURCHASE\", \"APPOINTMENT_BOOKING\"]")
		@NotNull(message = "권한 목록은 필수입니다.")
		Set<PermissionType> permissions
) {
}
