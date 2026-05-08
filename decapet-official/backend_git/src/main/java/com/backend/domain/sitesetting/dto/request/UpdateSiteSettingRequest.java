package com.backend.domain.sitesetting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "사이트 설정 수정 요청", example = """
        {
          "bannerText": "데카펫은 회원가입 승인 후 이용 가능합니다.",
          "bannerEnabled": true,
          "companyInfoHtml": "<p><strong>주식회사 비트진컴패니언</strong></p>",
          "productSectionTitle": "사료",
          "medicineSectionTitle": "진료 예약"
        }""")
public record UpdateSiteSettingRequest(
        @Schema(description = "상단 배너 문구", example = "데카펫은 회원가입 승인 후 이용 가능합니다.")
        @Size(max = 200, message = "배너 문구는 200자 이하여야 합니다.")
        String bannerText,

        @Schema(description = "상단 배너 노출 여부", example = "true")
        Boolean bannerEnabled,

        @Schema(description = "회사 정보 HTML (리치 텍스트)")
        @Size(max = 10000, message = "회사 정보는 10000자 이하여야 합니다.")
        String companyInfoHtml,

        @Schema(description = "상품 섹션 타이틀", example = "사료")
        @Size(max = 50, message = "상품 섹션 타이틀은 50자 이하여야 합니다.")
        String productSectionTitle,

        @Schema(description = "의약품 섹션 타이틀", example = "진료 예약")
        @Size(max = 50, message = "의약품 섹션 타이틀은 50자 이하여야 합니다.")
        String medicineSectionTitle,

        @Schema(description = "환불정책 HTML (리치 텍스트)")
        @Size(max = 10000, message = "환불정책은 10000자 이하여야 합니다.")
        String refundPolicyHtml,

        @Schema(description = "배송비 (원)", example = "3000")
        Integer shippingFee,

        @Schema(description = "무료배송 기준금액 (원)", example = "100000")
        Integer freeShippingThreshold,

        @Schema(description = "도서산간 추가배송비 (원)", example = "3000")
        Integer extraShippingFee,

        @Schema(description = "배송정보 안내 HTML (리치 텍스트)")
        @Size(max = 10000, message = "배송정보 안내는 10000자 이하여야 합니다.")
        String shippingInfoHtml
) {
}
