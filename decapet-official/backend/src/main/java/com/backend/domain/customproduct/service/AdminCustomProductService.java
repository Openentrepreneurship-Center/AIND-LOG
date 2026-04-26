package com.backend.domain.customproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import com.backend.domain.customproduct.dto.mapper.CustomProductResponseMapper;
import com.backend.domain.customproduct.dto.request.ApproveCustomProductRequest;
import com.backend.domain.customproduct.dto.request.UpdateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.entity.CustomProductStatus;
import com.backend.domain.customproduct.repository.CustomProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomProductService {

    private final CustomProductRepository customProductRepository;
    private final CustomProductResponseMapper customProductResponseMapper;

    public CustomProductListResponse getPendingCustomProducts(Pageable pageable) {
        Page<CustomProduct> page = customProductRepository.findByStatusAndDeletedAtIsNull(
                CustomProductStatus.PENDING, pageable);
        return customProductResponseMapper.toListResponse(page);
    }

    public CustomProductListResponse getAllCustomProducts(String searchText, Pageable pageable) {
        Specification<CustomProduct> spec = buildSpecification(searchText);
        Page<CustomProduct> page = customProductRepository.findAll(spec, pageable);
        return customProductResponseMapper.toListResponse(page);
    }

    private Specification<CustomProduct> buildSpecification(String searchText) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                var userJoin = root.join("user", JoinType.LEFT);
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(userJoin.get("name")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public CustomProductResponse getCustomProduct(String customProductId) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional
    public CustomProductResponse updateCustomProduct(String customProductId, UpdateCustomProductRequest request) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        customProduct.update(request.name(), request.description(), request.approvedPrice(),
                request.weight(), request.quantity(), request.allowMultiple(),
                request.additionalInfo(), request.stockQuantity(), request.basePrice());
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional
    public CustomProductResponse approveCustomProduct(String customProductId, ApproveCustomProductRequest request) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        customProduct.approve(request.approvedPrice(), request.allowMultiple(), request.stockQuantity());
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional
    public CustomProductResponse rejectCustomProduct(String customProductId) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        customProduct.reject();
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional
    public CustomProductResponse revertCustomProduct(String customProductId) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        customProduct.revertToPending();
        return customProductResponseMapper.toResponse(customProduct);
    }

    @Transactional
    public void deleteCustomProduct(String customProductId) {
        CustomProduct customProduct = customProductRepository.findByIdOrThrow(customProductId);
        customProductRepository.delete(customProduct);
    }
}
