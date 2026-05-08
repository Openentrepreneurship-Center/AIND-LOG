package com.backend.domain.cart.dto.request;

import com.backend.global.common.ItemType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "아이템 타입을 입력해주세요.")
        ItemType itemType,

        @NotBlank(message = "아이템 ID를 입력해주세요.")
        String itemId,

        @NotNull(message = "수량을 입력해주세요.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
