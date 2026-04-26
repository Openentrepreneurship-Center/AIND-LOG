package com.backend.domain.sitesetting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.sitesetting.dto.request.UpdateSiteSettingRequest;
import com.backend.domain.sitesetting.dto.response.SiteSettingResponse;
import com.backend.domain.sitesetting.service.AdminSiteSettingService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/site-settings")
@RequiredArgsConstructor
public class AdminSiteSettingController implements AdminSiteSettingApi {

    private final AdminSiteSettingService adminSiteSettingService;

    @GetMapping
    public ResponseEntity<SuccessResponse> getSiteSetting() {
        SiteSettingResponse response = adminSiteSettingService.getSiteSetting();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SITE_SETTING_GET_SUCCESS, response));
    }

    @PutMapping
    public ResponseEntity<SuccessResponse> updateSiteSetting(@RequestBody @Valid UpdateSiteSettingRequest request) {
        SiteSettingResponse response = adminSiteSettingService.updateSiteSetting(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }
}
