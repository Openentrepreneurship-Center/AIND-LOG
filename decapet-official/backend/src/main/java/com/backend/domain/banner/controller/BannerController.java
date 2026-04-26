package com.backend.domain.banner.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.banner.dto.response.MainBannerResponse;
import com.backend.domain.banner.service.BannerService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController implements BannerApi {

    private final BannerService bannerService;

    @Override
    @GetMapping("/main")
    public ResponseEntity<SuccessResponse> getMainBanners() {
        List<MainBannerResponse> response = bannerService.getMainBanners();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.BANNER_LIST_SUCCESS, response));
    }
}
