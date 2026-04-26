package com.backend.domain.product.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.product.dto.request.CreateProductRequest;
import com.backend.domain.product.entity.Product;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request, String imageUrl) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .price(request.price())
                .weight(request.weight())
                .stockQuantity(request.stockQuantity())
                .expirationDate(request.expirationDate())
                .imageUrl(imageUrl)
                .allowMultiple(request.allowMultiple())
                .additionalInfo(request.additionalInfo())
                .build();
    }
}
