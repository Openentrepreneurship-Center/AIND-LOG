package com.backend.domain.user.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_term_consents")
public class UserTermConsent {

    @EmbeddedId
    private UserTermConsentId id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    @Builder
    public UserTermConsent(String termId, int version) {
        this.id = new UserTermConsentId(null, termId, version);
        this.agreedAt = DateTimeUtil.now();
    }

    public String getTermId() {
        return this.id != null ? this.id.getTermId() : null;
    }

    public int getVersion() {
        return this.id != null ? this.id.getVersion() : 0;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    public static class UserTermConsentId implements Serializable {

        @Column(length = 26)
        private String userId;

        @Column(length = 26)
        private String termId;

        private int version;

        public UserTermConsentId(String userId, String termId, int version) {
            this.userId = userId;
            this.termId = termId;
            this.version = version;
        }

        void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
