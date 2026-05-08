package com.backend.domain.sitesetting.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.sitesetting.dto.response.SiteSettingResponse;
import com.backend.domain.sitesetting.entity.SiteSetting;

@Component
public class SiteSettingResponseMapper {

    public SiteSettingResponse toResponse(SiteSetting siteSetting) {
        return new SiteSettingResponse(
                siteSetting.getId(),
                siteSetting.getBannerText(),
                siteSetting.getBannerEnabled(),
                siteSetting.getCompanyInfoHtml(),
                siteSetting.getProductSectionTitle(),
                siteSetting.getMedicineSectionTitle(),
                siteSetting.getRefundPolicyHtml(),
                siteSetting.getShippingFee(),
                siteSetting.getFreeShippingThreshold(),
                siteSetting.getExtraShippingFee(),
                siteSetting.getShippingInfoHtml()
        );
    }
}
