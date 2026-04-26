package com.backend.domain.product.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.product.dto.response.FeaturedProductResponse;
import com.backend.domain.product.dto.response.ProductDetailResponse;
import com.backend.domain.product.dto.response.ProductListResponse;
import com.backend.domain.product.service.ProductService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    @GetMapping("/featured")
    public ResponseEntity<SuccessResponse> getFeaturedProducts() {
        List<FeaturedProductResponse> response = productService.getFeaturedProducts();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ProductListResponse response = productService.getProducts(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponse> getProduct(@PathVariable String productId) {
        ProductDetailResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.PRODUCT_GET_SUCCESS, response));
    }
}
