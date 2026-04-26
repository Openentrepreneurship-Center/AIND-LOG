package com.backend.domain.banner.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "banners", indexes = {
    @Index(name = "idx_banners_display_order", columnList = "display_order")
})
@SQLRestriction("deleted_at IS NULL")
public class Banner extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int displayOrder;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Builder
    public Banner(String title, String imageUrl,
                  int displayOrder, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void update(String title, String imageUrl,
                       LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
