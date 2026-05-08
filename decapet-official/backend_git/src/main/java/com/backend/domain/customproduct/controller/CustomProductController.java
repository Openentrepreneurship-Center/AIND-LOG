package com.backend.domain.customproduct.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.customproduct.dto.request.CreateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.domain.customproduct.service.CustomProductService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/custom-products")
@RequiredArgsConstructor
public class CustomProductController implements CustomProductApi {

    private final CustomProductService customProductService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createCustomProduct(
            @AuthenticationPrincipal String userId,
            @RequestPart("data") @Valid CreateCustomProductRequest request,
            @RequestPart("image") MultipartFile image) {
        CustomProductResponse response = customProductService.createCustomProduct(userId, request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMyCustomProducts(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomProductListResponse response = customProductService.getMyCustomProducts(userId, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{customProductId}")
    public ResponseEntity<SuccessResponse> getCustomProduct(
            @AuthenticationPrincipal String userId,
            @PathVariable String customProductId) {
        CustomProductResponse response = customProductService.getCustomProduct(userId, customProductId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_GET_SUCCESS, response));
    }
}
