package com.backend.domain.product.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.backend.domain.product.exception.InsufficientStockException;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean allowMultiple = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> additionalInfo;

    @Builder
    public Product(String name, String description, BigDecimal basePrice, BigDecimal price,
            BigDecimal weight, Integer stockQuantity, LocalDate expirationDate, String imageUrl,
            Boolean allowMultiple, List<String> additionalInfo) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.price = price;
        this.weight = weight;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
        this.allowMultiple = allowMultiple != null ? allowMultiple : true;
        this.additionalInfo = additionalInfo;
    }

    public boolean isAvailable() {
        return this.stockQuantity > 0;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new InsufficientStockException();
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void update(String name, String description, BigDecimal basePrice, BigDecimal price,
                       BigDecimal weight, Integer stockQuantity, LocalDate expirationDate,
                       String imageUrl, Boolean allowMultiple, List<String> additionalInfo) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.price = price;
        this.weight = weight;
        this.stockQuantity = stockQuantity;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
        this.allowMultiple = allowMultiple != null ? allowMultiple : true;
        this.additionalInfo = additionalInfo;
    }
}
