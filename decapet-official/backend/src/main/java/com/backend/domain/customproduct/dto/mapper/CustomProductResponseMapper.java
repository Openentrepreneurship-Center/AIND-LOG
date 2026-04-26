package com.backend.domain.customproduct.dto.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.domain.customproduct.entity.CustomProduct;

@Component
public class CustomProductResponseMapper {

    public CustomProductResponse toResponse(CustomProduct customProduct) {
        var user = customProduct.getUser();
        var pet = customProduct.getPet();

        return new CustomProductResponse(
                customProduct.getId(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                pet != null ? pet.getId() : null,
                pet != null ? pet.getName() : "(삭제된 반려동물)",
                pet != null ? pet.getGender() : null,
                pet != null ? pet.getBirthdate() : null,
                pet != null ? pet.getWeight() : null,
                customProduct.getName(),
                customProduct.getDescription(),
                customProduct.getRequestedPrice(),
                customProduct.getApprovedPrice(),
                customProduct.getBasePrice(),
                customProduct.getWeight(),
                customProduct.getQuantity(),
                customProduct.getImageUrl(),
                customProduct.getStatus(),
                customProduct.getAllowMultiple(),
                customProduct.getStockQuantity(),
                customProduct.getAdditionalInfo(),
                customProduct.getExpirationDate(),
                customProduct.getCreatedAt()
        );
    }

    public CustomProductListResponse toListResponse(Page<CustomProduct> page) {
        List<CustomProductResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new CustomProductListResponse(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
