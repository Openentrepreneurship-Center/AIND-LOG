package com.backend.domain.medicine.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.backend.domain.medicine.dto.internal.QuestionnaireItem;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "medicines", indexes = {
    @Index(name = "idx_medicines_type", columnList = "type")
})
@SQLRestriction("deleted_at IS NULL")
public class Medicine extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicineType type;

    @Column(precision = 5, scale = 2)
    private BigDecimal minWeight;

    @Column(precision = 5, scale = 2)
    private BigDecimal maxWeight;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<QuestionnaireItem> questionnaire;

    private Integer monthsPerUnit;

    @Column(precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicineSalesStatus salesStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_info", columnDefinition = "jsonb")
    private List<String> additionalInfo;

    @Builder
    public Medicine(String name, String description, BigDecimal price, MedicineType type,
            BigDecimal minWeight, BigDecimal maxWeight, LocalDate expirationDate,
            String imageUrl, List<QuestionnaireItem> questionnaire, Integer monthsPerUnit,
            BigDecimal salePrice, MedicineSalesStatus salesStatus, List<String> additionalInfo) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
        this.questionnaire = questionnaire;
        this.monthsPerUnit = monthsPerUnit;
        this.salePrice = salePrice;
        this.salesStatus = salesStatus != null ? salesStatus : MedicineSalesStatus.ON_SALE;
        this.additionalInfo = additionalInfo;
    }

    public void update(String name, String description, BigDecimal price, MedicineType type,
            BigDecimal minWeight, BigDecimal maxWeight, LocalDate expirationDate,
            String imageUrl, List<QuestionnaireItem> questionnaire, Integer monthsPerUnit,
            BigDecimal salePrice, MedicineSalesStatus salesStatus, List<String> additionalInfo) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
        this.questionnaire = questionnaire;
        this.monthsPerUnit = monthsPerUnit;
        this.salePrice = salePrice;
        this.salesStatus = salesStatus;
        this.additionalInfo = additionalInfo;
    }

}
