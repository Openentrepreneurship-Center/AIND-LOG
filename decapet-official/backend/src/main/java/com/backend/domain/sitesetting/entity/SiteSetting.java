package com.backend.domain.sitesetting.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "site_settings")
@EntityListeners(AuditingEntityListener.class)
public class SiteSetting {

    @Id
    @Column(length = 26)
    private String id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 상단 배너
    @Column(length = 500)
    private String bannerText;

    @Column(nullable = false)
    private Boolean bannerEnabled = true;

    // 회사 정보 (리치 텍스트 HTML)
    @Column(columnDefinition = "text")
    private String companyInfoHtml;

    // 섹션 타이틀
    @Column(length = 100)
    private String productSectionTitle;

    @Column(length = 100)
    private String medicineSectionTitle;

    // 환불정책 (리치 텍스트 HTML)
    @Column(columnDefinition = "text")
    private String refundPolicyHtml;

    // 배송비 설정
    @Column(nullable = false)
    private Integer shippingFee = 3000;

    @Column(nullable = false)
    private Integer freeShippingThreshold = 100000;

    @Column(nullable = false)
    private Integer extraShippingFee = 3000;

    // 배송정보 안내 (리치 텍스트 HTML)
    @Column(columnDefinition = "text")
    private String shippingInfoHtml;

    public void update(String bannerText, Boolean bannerEnabled,
                       String companyInfoHtml,
                       String productSectionTitle, String medicineSectionTitle,
                       String refundPolicyHtml,
                       Integer shippingFee, Integer freeShippingThreshold,
                       Integer extraShippingFee,
                       String shippingInfoHtml) {
        this.bannerText = bannerText;
        this.bannerEnabled = bannerEnabled;
        this.companyInfoHtml = companyInfoHtml;
        this.productSectionTitle = productSectionTitle;
        this.medicineSectionTitle = medicineSectionTitle;
        this.refundPolicyHtml = refundPolicyHtml;
        if (shippingFee != null) this.shippingFee = shippingFee;
        if (freeShippingThreshold != null) this.freeShippingThreshold = freeShippingThreshold;
        if (extraShippingFee != null) this.extraShippingFee = extraShippingFee;
        this.shippingInfoHtml = shippingInfoHtml;
    }
}
