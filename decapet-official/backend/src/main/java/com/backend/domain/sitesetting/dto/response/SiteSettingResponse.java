package com.backend.domain.sitesetting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사이트 설정 응답")
public record SiteSettingResponse(
        @Schema(description = "설정 ID", example = "01JJSITESETTING000001")
        String id,

        @Schema(description = "상단 배너 문구", example = "데카펫은 회원가입 승인 후 이용 가능합니다.")
        String bannerText,

        @Schema(description = "상단 배너 노출 여부", example = "true")
        Boolean bannerEnabled,

        @Schema(description = "회사 정보 HTML (리치 텍스트)")
        String companyInfoHtml,

        @Schema(description = "상품 섹션 타이틀", example = "사료")
        String productSectionTitle,

        @Schema(description = "의약품 섹션 타이틀", example = "진료 예약")
        String medicineSectionTitle,

        @Schema(description = "환불정책 HTML (리치 텍스트)")
        String refundPolicyHtml,

        @Schema(description = "배송비 (원)", example = "3000")
        Integer shippingFee,

        @Schema(description = "무료배송 기준금액 (원)", example = "100000")
        Integer freeShippingThreshold,

        @Schema(description = "도서산간 추가배송비 (원)", example = "3000")
        Integer extraShippingFee,

        @Schema(description = "배송정보 안내 HTML (리치 텍스트)")
        String shippingInfoHtml
) {
}
