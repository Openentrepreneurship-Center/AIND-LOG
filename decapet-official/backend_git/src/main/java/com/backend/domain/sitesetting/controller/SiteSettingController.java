package com.backend.domain.sitesetting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.sitesetting.dto.response.SiteSettingResponse;
import com.backend.domain.sitesetting.service.SiteSettingService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/site-settings")
@RequiredArgsConstructor
public class SiteSettingController implements SiteSettingApi {

    private final SiteSettingService siteSettingService;

    @GetMapping
    public ResponseEntity<SuccessResponse> getSiteSetting() {
        SiteSettingResponse response = siteSettingService.getSiteSetting();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SITE_SETTING_GET_SUCCESS, response));
    }
}
