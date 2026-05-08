package com.backend.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "배송지 정보를 입력해주세요.")
        @Valid
        ShippingAddressRequest shippingAddress
) {
}
