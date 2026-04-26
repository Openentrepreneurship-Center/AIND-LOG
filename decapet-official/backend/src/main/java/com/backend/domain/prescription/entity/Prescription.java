package com.backend.domain.prescription.entity;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.backend.domain.pet.entity.Pet;
import com.backend.domain.prescription.exception.InvalidPrescriptionPriceException;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_prescriptions_user", columnList = "user_id"),
    @Index(name = "idx_prescriptions_pet", columnList = "pet_id"),
    @Index(name = "idx_prescriptions_status", columnList = "status")
})
@SQLRestriction("deleted_at IS NULL")
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionType type;

    // 간단 신청용 필드
    @Column(columnDefinition = "TEXT")
    private String description;

    // 상세 신청용 필드
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attachmentUrls;

    @Column(length = 100)
    private String medicineName;

    @Column(length = 50)
    private String medicineWeight;

    @Column(length = 100)
    private String dosageFrequency;

    @Column(length = 50)
    private String dosagePeriod;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 512)
    private String imageUrl;

    @Builder
    public Prescription(User user, Pet pet, PrescriptionType type, String description,
                        List<String> attachmentUrls, String medicineName, String medicineWeight,
                        String dosageFrequency, String dosagePeriod, String additionalNotes) {
        this.user = user;
        this.pet = pet;
        this.type = type;
        this.description = description;
        this.attachmentUrls = attachmentUrls;
        this.medicineName = medicineName;
        this.medicineWeight = medicineWeight;
        this.dosageFrequency = dosageFrequency;
        this.dosagePeriod = dosagePeriod;
        this.additionalNotes = additionalNotes;
        this.status = PrescriptionStatus.PENDING;
    }

    public void approve(BigDecimal price, String imageUrl) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPrescriptionPriceException();
        }
        this.status = PrescriptionStatus.APPROVED;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public void reject() {
        this.status = PrescriptionStatus.REJECTED;
    }

    public void revertToPending() {
        this.status = PrescriptionStatus.PENDING;
        this.price = null;
        this.imageUrl = null;
    }

    public void updateMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void anonymize() {
        this.description = null;
        this.attachmentUrls = null;
        this.additionalNotes = null;
    }
}
