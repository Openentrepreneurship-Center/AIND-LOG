package com.backend.domain.pet.entity;

import org.hibernate.annotations.SQLRestriction;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "pet_vets", indexes = {
    @Index(name = "idx_pet_vets_pet", columnList = "pet_id")
})
@SQLRestriction("deleted_at IS NULL")
public class PetVet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 255)
    private String hospitalName;

    @Column(nullable = false, length = 100)
    private String vetName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private VetPosition vetPosition;

    @Builder
    public PetVet(Pet pet, String hospitalName, String vetName, VetPosition vetPosition) {
        this.pet = pet;
        this.hospitalName = hospitalName;
        this.vetName = vetName;
        this.vetPosition = vetPosition;
    }

    public void update(String hospitalName, String vetName, VetPosition vetPosition) {
        this.hospitalName = hospitalName;
        this.vetName = vetName;
        this.vetPosition = vetPosition;
    }
}
