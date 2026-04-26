package com.backend.domain.customproduct.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.customproduct.dto.mapper.CustomProductMapper;
import com.backend.domain.customproduct.dto.mapper.CustomProductResponseMapper;
import com.backend.domain.customproduct.dto.request.CreateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.exception.CustomProductNotOwnedException;
import com.backend.domain.customproduct.repository.CustomProductRepository;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.pet.exception.PetPermissionDeniedException;
import com.backend.domain.pet.service.PetService;
import com.backend.domain.product.exception.ImageRequiredException;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomProductService {

    private static final String CUSTOM_PRODUCT_IMAGE_DIRECTORY = "products/custom";

    private final CustomProductRepository customProductRepository;
    private final UserRepository userRepository;
    private final PetService petService;
    private final CustomProductMapper customProductMapper;
    private final CustomProductResponseMapper customProductResponseMapper;
    private final S3Service s3Service;

    @Transactional
    public CustomProductResponse createCustomProduct(String userId, CreateCustomProductRequest request, MultipartFile image) {
        validateImage(image);

        Pet pet = petService.getPetEntity(request.petId());
        validatePetOwnership(userId, pet);

        User user = userRepository.getReferenceById(userId);
        String imageUrl = s3Service.uploadFile(image, CUSTOM_PRODUCT_IMAGE_DIRECTORY);

        CustomProduct customProduct = customProductMapper.toEntity(request, imageUrl, user, pet);
        CustomProduct saved = customProductRepository.save(customProduct);

        return customProductResponseMapper.toResponse(saved);
    }

    public CustomProductListResponse getMyCustomProducts(String userId, Pageable pageable) {
        Page<CustomProduct> page = customProductRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        return customProductResponseMapper.toListResponse(page);
    }

    public CustomProductResponse getCustomProduct(String userId, String customProductId) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        validateOwnership(customProduct, userId);
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional(readOnly = true)
    public CustomProduct getByIdOrThrow(String customProductId) {
        return customProductRepository.findByIdOrThrow(customProductId);
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ImageRequiredException();
        }
    }

    private void validateOwnership(CustomProduct customProduct, String userId) {
        if (customProduct.getUser() == null || !customProduct.getUser().getId().equals(userId)) {
            throw new CustomProductNotOwnedException();
        }
    }

    private void validatePetOwnership(String userId, Pet pet) {
        if (pet.getUser() == null || !pet.getUser().getId().equals(userId)) {
            throw new PetPermissionDeniedException();
        }
    }
}
