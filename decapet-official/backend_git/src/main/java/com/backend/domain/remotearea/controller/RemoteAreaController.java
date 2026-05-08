package com.backend.domain.remotearea.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.remotearea.repository.RemoteAreaZipCodeRepository;
import com.backend.domain.sitesetting.entity.SiteSetting;
import com.backend.domain.sitesetting.repository.SiteSettingRepository;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/remote-area")
@RequiredArgsConstructor
public class RemoteAreaController {

    private final RemoteAreaZipCodeRepository remoteAreaZipCodeRepository;
    private final SiteSettingRepository siteSettingRepository;

    @GetMapping("/check")
    public ResponseEntity<SuccessResponse> checkRemoteArea(@RequestParam String zipCode) {
        boolean isRemoteArea = remoteAreaZipCodeRepository.existsByZipCode(zipCode);
        SiteSetting siteSetting = siteSettingRepository.findFirstOrThrow();
        int extraShippingFee = isRemoteArea ? siteSetting.getExtraShippingFee() : 0;

        record RemoteAreaCheckResponse(boolean isRemoteArea, int extraShippingFee) {}

        return ResponseEntity.ok(SuccessResponse.of(
                SuccessCode.REMOTE_AREA_CHECK_SUCCESS,
                new RemoteAreaCheckResponse(isRemoteArea, extraShippingFee)
        ));
    }
}
