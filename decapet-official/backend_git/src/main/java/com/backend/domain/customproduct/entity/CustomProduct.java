package com.backend.domain.customproduct.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import com.backend.domain.customproduct.exception.CustomProductAlreadyApprovedException;
import com.backend.domain.customproduct.exception.CustomProductAlreadyRejectedException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "custom_products")
@SQLRestriction("deleted_at IS NULL")
public class CustomProduct extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal approvedPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    private Integer quantity;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomProductStatus status;

    private Boolean allowMultiple;

    @Column(nullable = false)
    private int stockQuantity;

    private LocalDateTime approvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> additionalInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Builder
    public CustomProduct(String name, String description, BigDecimal requestedPrice,
                         String imageUrl, BigDecimal weight, Integer quantity, User user,
                         Pet pet, List<String> additionalInfo) {
        this.name = name;
        this.description = description;
        this.requestedPrice = requestedPrice;
        this.imageUrl = imageUrl;
        this.weight = weight;
        this.quantity = quantity;
        this.status = CustomProductStatus.PENDING;
        this.user = user;
        this.pet = pet;
        this.additionalInfo = additionalInfo;
    }

    /**
     * 만료일 조회 (승인일 + 1년, 미승인 시 null)
     */
    public LocalDateTime getExpirationDate() {
        return this.approvedAt != null ? this.approvedAt.plusYears(1) : null;
    }

    /**
     * 커스텀 상품 수정 (관리자)
     */
    public void update(String name, String description, BigDecimal approvedPrice,
                       BigDecimal weight, Integer quantity, Boolean allowMultiple,
                       List<String> additionalInfo, Integer stockQuantity, BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.approvedPrice = approvedPrice;
        this.basePrice = basePrice;
        this.weight = weight;
        this.quantity = quantity;
        this.allowMultiple = allowMultiple;
        this.additionalInfo = additionalInfo;
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
    }

    /**
     * 커스텀 상품 승인 (관리자가 가격과 수량추가 가능 여부 결정)
     */
    public void approve(BigDecimal approvedPrice, Boolean allowMultiple, int stockQuantity) {
        this.approvedPrice = approvedPrice;
        this.allowMultiple = allowMultiple != null ? allowMultiple : false;
        this.stockQuantity = stockQuantity;
        this.status = CustomProductStatus.APPROVED;
        this.approvedAt = DateTimeUtil.now();
    }

    /**
     * 커스텀 상품 거절
     */
    public void reject() {
        this.status = CustomProductStatus.REJECTED;
    }

    /**
     * 커스텀 상품 상태 복원 (PENDING으로)
     */
    public void revertToPending() {
        this.status = CustomProductStatus.PENDING;
        this.approvedPrice = null;
        this.approvedAt = null;
        this.allowMultiple = null;
        this.stockQuantity = 0;
    }

    public boolean isInStock() {
        return this.stockQuantity > 0;
    }

    public void anonymize() {
        this.description = null;
        this.imageUrl = null;
    }
}
