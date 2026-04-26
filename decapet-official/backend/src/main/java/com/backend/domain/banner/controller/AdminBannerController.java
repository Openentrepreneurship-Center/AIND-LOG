package com.backend.domain.banner.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.banner.dto.request.CreateBannerRequest;
import com.backend.domain.banner.dto.request.ReorderBannersRequest;
import com.backend.domain.banner.dto.request.UpdateBannerRequest;
import com.backend.domain.banner.dto.response.BannerResponse;
import com.backend.domain.banner.service.AdminBannerService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController implements AdminBannerApi {

    private final AdminBannerService adminBannerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createBanner(
            @RequestPart("image") MultipartFile image,
            @RequestPart("data") @Valid CreateBannerRequest request) {
        BannerResponse response = adminBannerService.createBanner(request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_CREATE_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAllBanners(Pageable pageable) {
        PageResponse<BannerResponse> response = adminBannerService.getAllBanners(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.BANNER_LIST_SUCCESS, response));
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<SuccessResponse> getBanner(@PathVariable String bannerId) {
        BannerResponse response = adminBannerService.getBanner(bannerId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.BANNER_LIST_SUCCESS, response));
    }

    @PutMapping(value = "/{bannerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateBanner(
            @PathVariable String bannerId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") @Valid UpdateBannerRequest request) {
        BannerResponse response = adminBannerService.updateBanner(bannerId, request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<SuccessResponse> deleteBanner(@PathVariable String bannerId) {
        adminBannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }

    @PutMapping("/reorder")
    public ResponseEntity<SuccessResponse> reorderBanners(@RequestBody @Valid ReorderBannersRequest request) {
        List<BannerResponse> response = adminBannerService.reorderBanners(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }
}
