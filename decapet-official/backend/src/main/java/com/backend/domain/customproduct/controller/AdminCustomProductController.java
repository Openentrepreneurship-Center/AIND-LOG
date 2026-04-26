package com.backend.domain.customproduct.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.customproduct.dto.request.ApproveCustomProductRequest;
import com.backend.domain.customproduct.dto.request.UpdateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.domain.customproduct.service.AdminCustomProductService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/custom-products")
@RequiredArgsConstructor
public class AdminCustomProductController implements AdminCustomProductApi {

    private final AdminCustomProductService adminCustomProductService;

    @Override
    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse> getPendingCustomProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomProductListResponse response = adminCustomProductService.getPendingCustomProducts(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllCustomProducts(
            @RequestParam(required = false) String searchText,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomProductListResponse response = adminCustomProductService.getAllCustomProducts(searchText, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{customProductId}")
    public ResponseEntity<SuccessResponse> getCustomProduct(@PathVariable String customProductId) {
        CustomProductResponse response = adminCustomProductService.getCustomProduct(customProductId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_GET_SUCCESS, response));
    }

    @Override
    @PutMapping("/{customProductId}")
    public ResponseEntity<SuccessResponse> updateCustomProduct(
            @PathVariable String customProductId,
            @RequestBody @Valid UpdateCustomProductRequest request) {
        CustomProductResponse response = adminCustomProductService.updateCustomProduct(customProductId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }

    @Override
    @PostMapping("/{customProductId}/approve")
    public ResponseEntity<SuccessResponse> approveCustomProduct(
            @PathVariable String customProductId,
            @RequestBody @Valid ApproveCustomProductRequest request) {
        CustomProductResponse response = adminCustomProductService.approveCustomProduct(customProductId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_APPROVE_SUCCESS, response));
    }

    @Override
    @PostMapping("/{customProductId}/reject")
    public ResponseEntity<SuccessResponse> rejectCustomProduct(@PathVariable String customProductId) {
        CustomProductResponse response = adminCustomProductService.rejectCustomProduct(customProductId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CUSTOM_PRODUCT_REJECT_SUCCESS, response));
    }

    @Override
    @PostMapping("/{customProductId}/revert")
    public ResponseEntity<SuccessResponse> revertCustomProduct(@PathVariable String customProductId) {
        CustomProductResponse response = adminCustomProductService.revertCustomProduct(customProductId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_PRODUCT_REVERT_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{customProductId}")
    public ResponseEntity<SuccessResponse> deleteCustomProduct(@PathVariable String customProductId) {
        adminCustomProductService.deleteCustomProduct(customProductId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }
}
