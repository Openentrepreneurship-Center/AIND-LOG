package com.backend.domain.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDeliveryRequest(
        @NotBlank(message = "주문 ID를 입력해주세요.")
        String orderId,

        @NotBlank(message = "수령인 이름을 입력해주세요.")
        String recipientName,

        @NotBlank(message = "연락처를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)")
        String phoneNumber,

        @NotBlank(message = "우편번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipCode,

        @NotBlank(message = "주소를 입력해주세요.")
        @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
        String address,

        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
        String detailAddress,

        String deliveryNote
) {
}
