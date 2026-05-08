package com.backend.domain.breed.entity;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "breeds", indexes = {
    @Index(name = "idx_breeds_species", columnList = "species")
})
public class Breed {

    @Id
    @Column(length = 26)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public Breed(Species species, String name) {
        this.id = UlidGenerator.generate();
        this.species = species;
        this.name = name;
    }
}
