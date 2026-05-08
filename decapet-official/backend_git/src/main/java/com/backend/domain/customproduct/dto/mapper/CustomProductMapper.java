package com.backend.domain.customproduct.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.customproduct.dto.request.CreateCustomProductRequest;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.entity.User;

@Component
public class CustomProductMapper {

    public CustomProduct toEntity(CreateCustomProductRequest request, String imageUrl, User user, Pet pet) {
        return CustomProduct.builder()
                .name(request.name())
                .description(request.description())
                .requestedPrice(request.requestedPrice())
                .weight(request.weight())
                .quantity(request.quantity())
                .imageUrl(imageUrl)
                .user(user)
                .pet(pet)
                .additionalInfo(request.additionalInfo())
                .build();
    }
}
