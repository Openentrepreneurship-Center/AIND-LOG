package com.backend.domain.delivery.dto.response;

import java.util.List;

public record BulkTrackingResponse(
		int successCount,
		int failCount,
		List<FailureDetail> failures
) {
	public record FailureDetail(
			String orderNumber,
			String reason
	) {
	}
}
