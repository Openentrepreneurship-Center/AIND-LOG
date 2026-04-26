package com.backend.domain.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "배송 취소 요청")
public record CancelDeliveryRequest(
		@Schema(description = "취소 사유", example = "고객 요청")
		@NotBlank(message = "취소 사유는 필수입니다.")
		@Size(max = 500, message = "취소 사유는 500자 이하여야 합니다.")
		String reason
) {
}
