package com.backend.domain.order.dto.response;

import java.util.List;

public record OrderListResponse(
        List<OrderResponse> orders,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
