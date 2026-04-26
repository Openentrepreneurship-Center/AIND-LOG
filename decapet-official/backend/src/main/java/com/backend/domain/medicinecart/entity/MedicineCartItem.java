package com.backend.domain.medicinecart.entity;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.backend.domain.appointment.entity.QuestionnaireAnswer;
import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "medicine_cart_items", indexes = {
    @Index(name = "idx_medicine_cart_items_cart", columnList = "cart_id")
})
public class MedicineCartItem {

    @Id
    @Column(length = 26)
    private String id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private MedicineCart cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private MedicineItemType itemType;

    @Column(length = 26)
    private String medicineId;

    @Column(length = 26)
    private String prescriptionId;

    @Column(nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false, length = 26)
    private String petId;

    @Column(nullable = false, length = 100)
    private String petName;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<QuestionnaireAnswer> questionnaireAnswers;

    @Builder
    public MedicineCartItem(MedicineItemType itemType, String medicineId, String prescriptionId,
            String itemName, String petId, String petName, String imageUrl,
            BigDecimal unitPrice, Integer quantity, List<QuestionnaireAnswer> questionnaireAnswers) {
        this.id = UlidGenerator.generate();
        this.itemType = itemType != null ? itemType : MedicineItemType.MEDICINE;
        this.medicineId = medicineId;
        this.prescriptionId = prescriptionId;
        this.itemName = itemName;
        this.petId = petId;
        this.petName = petName;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.questionnaireAnswers = questionnaireAnswers;
    }

    /**
     * 아이템 ID 반환 (타입에 따라 medicineId 또는 prescriptionId)
     */
    public String getItemId() {
        return itemType == MedicineItemType.PRESCRIPTION ? prescriptionId : medicineId;
    }

    /**
     * 기존 코드 호환성을 위한 medicineName getter
     */
    public String getMedicineName() {
        return itemName;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity));
    }

    public void increaseQuantity(int additionalQuantity) {
        this.quantity += additionalQuantity;
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public boolean hasAllAffirmativeAnswers() {
        if (questionnaireAnswers == null || questionnaireAnswers.isEmpty()) {
            // 처방전은 문진 응답이 없어도 OK
            return itemType == MedicineItemType.PRESCRIPTION || false;
        }
        return questionnaireAnswers.stream().allMatch(QuestionnaireAnswer::isAnswer);
    }

    public boolean isMedicine() {
        return itemType == MedicineItemType.MEDICINE;
    }

    public boolean isPrescription() {
        return itemType == MedicineItemType.PRESCRIPTION;
    }
}
