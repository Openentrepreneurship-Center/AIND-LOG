package com.backend.domain.product.dto.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.backend.domain.product.dto.response.FeaturedProductResponse;
import com.backend.domain.product.dto.response.ProductDetailResponse;
import com.backend.domain.product.dto.response.ProductListResponse;
import com.backend.domain.product.dto.response.ProductResponse;
import com.backend.domain.product.dto.response.ProductSummaryResponse;
import com.backend.domain.product.entity.Product;

@Component
public class ProductResponseMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getPrice(),
                product.getWeight(),
                product.getStockQuantity(),
                product.getExpirationDate(),
                product.getImageUrl(),
                product.getAllowMultiple(),
                product.getAdditionalInfo(),
                product.isAvailable(),
                product.getCreatedAt()
        );
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getPrice(),
                product.getWeight(),
                product.getExpirationDate(),
                product.getImageUrl(),
                product.getAllowMultiple(),
                product.getAdditionalInfo()
        );
    }

    public ProductListResponse toListResponse(Page<Product> productPage) {
        List<ProductSummaryResponse> products = productPage.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new ProductListResponse(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getPrice(),
                product.getWeight(),
                product.getStockQuantity(),
                product.getExpirationDate(),
                product.getImageUrl(),
                product.getAllowMultiple(),
                product.getAdditionalInfo()
        );
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public FeaturedProductResponse toFeaturedResponse(Product product) {
        return new FeaturedProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getPrice(),
                product.getWeight(),
                product.getExpirationDate(),
                product.getImageUrl()
        );
    }

    public List<FeaturedProductResponse> toFeaturedResponseList(List<Product> products) {
        return products.stream()
                .map(this::toFeaturedResponse)
                .toList();
    }
}
