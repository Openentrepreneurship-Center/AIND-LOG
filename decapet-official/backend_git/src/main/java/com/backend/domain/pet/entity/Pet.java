package com.backend.domain.pet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.breed.entity.Breed;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;
import com.backend.global.util.UniqueNumberGenerator;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "pets", indexes = {
    @Index(name = "idx_pets_user_deleted", columnList = "user_id, deleted_at"),
    @Index(name = "idx_pets_breed", columnList = "breed_id")
})
@SQLRestriction("deleted_at IS NULL")
public class Pet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 12)
    private String uniqueNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id", nullable = false)
    private Breed breed;

    @Column(length = 100)
    private String customBreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false)
    private Boolean neutered;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    private LocalDateTime weightUpdatedAt;

    @Column(length = 15)
    private String registrationNumber;

    @Column(length = 500)
    private String photo;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetVet> vets = new ArrayList<>();

    @Builder
    public Pet(User user, String name, Breed breed, String customBreed,
               Gender gender, boolean neutered, LocalDate birthdate, BigDecimal weight,
               String registrationNumber) {
        this.uniqueNumber = UniqueNumberGenerator.generate();
        this.user = user;
        this.name = name;
        this.breed = breed;
        this.customBreed = customBreed;
        this.gender = gender;
        this.neutered = neutered;
        this.birthdate = birthdate;
        this.weight = weight;
        this.weightUpdatedAt = DateTimeUtil.now();
        this.registrationNumber = registrationNumber;
    }

    public void updateInfo(String name, Breed breed, String customBreed, Gender gender,
                           boolean neutered, LocalDate birthdate, String registrationNumber) {
        if (name != null) this.name = name;
        if (breed != null) this.breed = breed;
        this.customBreed = customBreed;
        if (gender != null) this.gender = gender;
        this.neutered = neutered;
        if (birthdate != null) this.birthdate = birthdate;
        this.registrationNumber = registrationNumber;
    }

    public String getBreedName() {
        if (customBreed != null && !customBreed.isBlank()) {
            return customBreed;
        }
        return breed != null ? breed.getName() : null;
    }

    public boolean canUpdateWeight() {
        if (weightUpdatedAt == null) return true;
        return weightUpdatedAt.plusDays(30).isBefore(DateTimeUtil.now());
    }

    public void updateWeight(BigDecimal newWeight) {
        this.weight = newWeight;
        this.weightUpdatedAt = DateTimeUtil.now();
    }

    public void updatePhoto(String photoUrl) {
        this.photo = photoUrl;
    }

    public void addVet(PetVet vet) {
        this.vets.add(vet);
    }

    public void removeVet(PetVet vet) {
        this.vets.remove(vet);
    }

    public void regenerateUniqueNumber() {
        this.uniqueNumber = UniqueNumberGenerator.generate();
    }

    public void anonymize() {
        this.registrationNumber = null;
        this.photo = null;
    }
}
