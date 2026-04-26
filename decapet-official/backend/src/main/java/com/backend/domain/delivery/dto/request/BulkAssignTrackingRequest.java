package com.backend.domain.delivery.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record BulkAssignTrackingRequest(
		@NotEmpty(message = "운송장 목록이 비어있습니다.")
		@Valid
		List<Item> items
) {
	public record Item(
			@NotBlank(message = "주문번호를 입력해주세요.")
			String orderNumber,

			@NotBlank(message = "운송장 번호를 입력해주세요.")
			String trackingNumber
	) {
	}
}
