package com.backend.domain.product.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.product.dto.request.CreateProductRequest;
import com.backend.domain.product.dto.request.UpdateProductRequest;
import com.backend.domain.product.dto.response.ProductListResponse;
import com.backend.domain.product.dto.response.ProductResponse;
import com.backend.domain.product.service.AdminProductService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController implements AdminProductApi {

    private final AdminProductService adminProductService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createProduct(
            @RequestPart("data") @Valid CreateProductRequest request,
            @RequestPart("image") MultipartFile image) {
        ProductResponse response = adminProductService.createProduct(request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponse> getProduct(@PathVariable String productId) {
        ProductResponse response = adminProductService.getProduct(productId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_GET_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllProducts(
            @RequestParam(required = false) String searchText,
            @PageableDefault(size = 10, sort = "expirationDate", direction = Sort.Direction.ASC) Pageable pageable) {
        ProductListResponse response = adminProductService.getAllProducts(searchText, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateProduct(
            @PathVariable String productId,
            @RequestPart("data") @Valid UpdateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponse response = adminProductService.updateProduct(productId, request, image);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse> deleteProduct(@PathVariable String productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }
}
