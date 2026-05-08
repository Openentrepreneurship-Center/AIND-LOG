package com.backend.domain.sitesetting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.sitesetting.dto.mapper.SiteSettingResponseMapper;
import com.backend.domain.sitesetting.dto.request.UpdateSiteSettingRequest;
import com.backend.domain.sitesetting.dto.response.SiteSettingResponse;
import com.backend.domain.sitesetting.entity.SiteSetting;
import com.backend.domain.sitesetting.repository.SiteSettingRepository;
import com.backend.global.aop.AdminAudit;
import com.backend.global.util.HtmlSanitizer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSiteSettingService {

    private final SiteSettingRepository siteSettingRepository;
    private final SiteSettingResponseMapper siteSettingResponseMapper;
    private final HtmlSanitizer htmlSanitizer;

    public SiteSettingResponse getSiteSetting() {
        SiteSetting siteSetting = siteSettingRepository.findFirstOrThrow();
        return siteSettingResponseMapper.toResponse(siteSetting);
    }

    @AdminAudit(action = "SITE_SETTING_UPDATE", targetType = "SiteSetting")
    @Transactional
    public SiteSettingResponse updateSiteSetting(UpdateSiteSettingRequest request) {
        SiteSetting siteSetting = siteSettingRepository.findFirstOrThrow();

        siteSetting.update(
                request.bannerText(),
                request.bannerEnabled(),
                htmlSanitizer.sanitize(request.companyInfoHtml()),
                request.productSectionTitle(),
                request.medicineSectionTitle(),
                htmlSanitizer.sanitize(request.refundPolicyHtml()),
                request.shippingFee(),
                request.freeShippingThreshold(),
                request.extraShippingFee(),
                htmlSanitizer.sanitize(request.shippingInfoHtml())
        );

        return siteSettingResponseMapper.toResponse(siteSetting);
    }
}
