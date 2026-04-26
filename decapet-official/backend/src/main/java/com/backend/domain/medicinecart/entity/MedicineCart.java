package com.backend.domain.medicinecart.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.user.entity.User;
import com.backend.global.util.UlidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "medicine_carts", uniqueConstraints = {
    @UniqueConstraint(name = "idx_medicine_carts_user", columnNames = {"user_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class MedicineCart {

    @Id
    @Column(length = 26)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "time_slot_id", length = 26)
    private String timeSlotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", insertable = false, updatable = false)
    private TimeSlot timeSlot;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MedicineCartItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MedicineCart(User user) {
        this.id = UlidGenerator.generate();
        this.user = user;
        this.items = new ArrayList<>();
    }

    public void addItem(MedicineCartItem item) {
        // 같은 타입 + 같은 반려동물 + 같은 아이템이 있는지 확인
        Optional<MedicineCartItem> existing = items.stream()
                .filter(i -> i.getItemType() == item.getItemType()
                          && i.getPetId().equals(item.getPetId())
                          && i.getItemId().equals(item.getItemId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().increaseQuantity(item.getQuantity());
        } else {
            item.setCart(this);
            items.add(item);
        }
    }

    public void updateItemQuantity(MedicineItemType itemType, String petId, String itemId, int quantity) {
        items.stream()
                .filter(i -> i.getItemType() == itemType
                          && i.getPetId().equals(petId)
                          && i.getItemId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        removeItem(itemType, petId, itemId);
                    } else {
                        item.updateQuantity(quantity);
                    }
                });
    }

    public void removeItem(MedicineItemType itemType, String petId, String itemId) {
        items.removeIf(i -> i.getItemType() == itemType
                         && i.getPetId().equals(petId)
                         && i.getItemId().equals(itemId));
    }

    public void clear() {
        items.clear();
        this.timeSlotId = null;
    }

    public void setTimeSlotId(String timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(MedicineCartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(MedicineCartItem::getQuantity)
                .sum();
    }

    public boolean hasAllAffirmativeAnswers() {
        if (items.isEmpty()) {
            return false;
        }
        return items.stream().allMatch(MedicineCartItem::hasAllAffirmativeAnswers);
    }

    // 특정 반려동물의 아이템만 조회
    public List<MedicineCartItem> getItemsForPet(String petId) {
        return items.stream()
                .filter(i -> i.getPetId().equals(petId))
                .toList();
    }

    // 특정 반려동물의 아이템이 비어있는지 확인
    public boolean isEmptyForPet(String petId) {
        return getItemsForPet(petId).isEmpty();
    }

    // 특정 반려동물의 아이템들이 모두 문진 응답을 완료했는지 확인
    public boolean hasAllAffirmativeAnswersForPet(String petId) {
        List<MedicineCartItem> petItems = getItemsForPet(petId);
        if (petItems.isEmpty()) {
            return false;
        }
        return petItems.stream().allMatch(MedicineCartItem::hasAllAffirmativeAnswers);
    }

    // 특정 반려동물의 아이템만 삭제
    public void clearItemsForPet(String petId) {
        items.removeIf(i -> i.getPetId().equals(petId));
    }
}
