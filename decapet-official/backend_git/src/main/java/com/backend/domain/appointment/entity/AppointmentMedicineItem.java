package com.backend.domain.appointment.entity;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.backend.domain.medicinecart.entity.MedicineItemType;
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
@Table(name = "appointment_medicine_items", indexes = {
    @Index(name = "idx_appointment_medicine_items_appointment", columnList = "appointment_id")
})
public class AppointmentMedicineItem {

    @Id
    @Column(length = 26)
    private String id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private MedicineItemType itemType;

    @Column(length = 26)
    private String medicineId;

    @Column(length = 26)
    private String prescriptionId;

    @Column(nullable = false, length = 255)
    private String medicineName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(length = 512)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<QuestionnaireAnswer> questionnaireAnswers;

    @Builder
    public AppointmentMedicineItem(MedicineItemType itemType, String medicineId, String prescriptionId,
            String medicineName, BigDecimal unitPrice, Integer quantity, String imageUrl,
            List<QuestionnaireAnswer> questionnaireAnswers) {
        this.id = UlidGenerator.generate();
        this.itemType = itemType != null ? itemType : MedicineItemType.MEDICINE;
        this.medicineId = medicineId;
        this.prescriptionId = prescriptionId;
        this.medicineName = medicineName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.imageUrl = imageUrl;
        this.questionnaireAnswers = questionnaireAnswers;
    }

    /**
     * 아이템 ID 반환 (타입에 따라 medicineId 또는 prescriptionId)
     */
    public String getItemId() {
        return itemType == MedicineItemType.PRESCRIPTION ? prescriptionId : medicineId;
    }

    public boolean isMedicine() {
        return itemType == MedicineItemType.MEDICINE;
    }

    public boolean isPrescription() {
        return itemType == MedicineItemType.PRESCRIPTION;
    }
}
