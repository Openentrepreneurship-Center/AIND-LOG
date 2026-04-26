package com.backend.domain.product.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.product.dto.mapper.ProductResponseMapper;
import com.backend.domain.product.dto.response.FeaturedProductResponse;
import com.backend.domain.product.dto.response.ProductDetailResponse;
import com.backend.domain.product.dto.response.ProductListResponse;
import com.backend.domain.product.entity.Product;
import com.backend.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStockQuantityGreaterThan(0, pageable);
        return productResponseMapper.toListResponse(products);
    }

    @Transactional(readOnly = true)
    public List<FeaturedProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findRandomFeaturedProducts();
        return productResponseMapper.toFeaturedResponseList(products);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(String productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        return productResponseMapper.toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public Product getByIdOrThrow(String productId) {
        return productRepository.findByIdOrThrow(productId);
    }
}
