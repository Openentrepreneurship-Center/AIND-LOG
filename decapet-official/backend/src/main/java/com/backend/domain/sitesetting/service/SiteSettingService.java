package com.backend.domain.sitesetting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.sitesetting.dto.mapper.SiteSettingResponseMapper;
import com.backend.domain.sitesetting.dto.response.SiteSettingResponse;
import com.backend.domain.sitesetting.entity.SiteSetting;
import com.backend.domain.sitesetting.repository.SiteSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;
    private final SiteSettingResponseMapper siteSettingResponseMapper;

    public SiteSettingResponse getSiteSetting() {
        SiteSetting siteSetting = siteSettingRepository.findFirstOrThrow();
        return siteSettingResponseMapper.toResponse(siteSetting);
    }
}
