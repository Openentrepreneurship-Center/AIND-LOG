package com.backend.domain.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;

import com.backend.domain.cart.repository.CartRepository;
import com.backend.domain.product.dto.mapper.ProductMapper;
import com.backend.domain.product.dto.mapper.ProductResponseMapper;
import com.backend.domain.product.dto.request.CreateProductRequest;
import com.backend.domain.product.dto.request.UpdateProductRequest;
import com.backend.domain.product.dto.response.ProductListResponse;
import com.backend.domain.product.dto.response.ProductResponse;
import com.backend.domain.product.entity.Product;
import com.backend.domain.product.repository.ProductRepository;
import com.backend.global.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private static final String PRODUCT_IMAGE_DIRECTORY = "products";

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ProductMapper productMapper;
    private final ProductResponseMapper productResponseMapper;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;

    public ProductResponse createProduct(CreateProductRequest request, MultipartFile image) {
        String imageUrl = s3Service.uploadFile(image, PRODUCT_IMAGE_DIRECTORY);

        try {
            return transactionTemplate.execute(status -> {
                Product product = productMapper.toEntity(request, imageUrl);
                Product savedProduct = productRepository.save(product);
                return productResponseMapper.toResponse(savedProduct);
            });
        } catch (Exception e) {
            s3Service.deleteFile(imageUrl);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(String productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        return productResponseMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getAllProducts(String searchText, Pageable pageable) {
        Specification<Product> spec = buildSpecification(searchText);
        Page<Product> products = productRepository.findAll(spec, pageable);
        return productResponseMapper.toListResponse(products);
    }

    private Specification<Product> buildSpecification(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), like));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public ProductResponse updateProduct(String productId, UpdateProductRequest request, MultipartFile image) {
        Product product = productRepository.findByIdOrThrow(productId);
        String oldImageUrl = product.getImageUrl();

        String newImageUrl = (image != null && !image.isEmpty())
                ? s3Service.uploadFile(image, PRODUCT_IMAGE_DIRECTORY)
                : null;

        try {
            ProductResponse response = transactionTemplate.execute(status -> {
                String imageUrl = newImageUrl != null ? newImageUrl : oldImageUrl;
                product.update(
                        request.name(),
                        request.description(),
                        request.basePrice(),
                        request.price(),
                        request.weight(),
                        request.stockQuantity(),
                        request.expirationDate(),
                        imageUrl,
                        request.allowMultiple(),
                        request.additionalInfo()
                );
                return productResponseMapper.toResponse(product);
            });

            // DB 커밋 성공 후 이전 이미지 삭제
            if (newImageUrl != null && oldImageUrl != null) {
                s3Service.deleteFile(oldImageUrl);
            }
            return response;
        } catch (Exception e) {
            if (newImageUrl != null) {
                s3Service.deleteFile(newImageUrl);
            }
            throw e;
        }
    }

    @Transactional
    public void deleteProduct(String productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        String imageUrl = product.getImageUrl();
        cartRepository.deleteCartItemsByProductId(productId);
        product.delete();

        // soft delete 후 S3 정리 (best-effort)
        if (imageUrl != null) {
            s3Service.deleteFile(imageUrl);
        }
    }
}
