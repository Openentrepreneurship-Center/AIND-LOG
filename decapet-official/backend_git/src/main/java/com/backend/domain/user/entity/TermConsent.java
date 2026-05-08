package com.backend.domain.user.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor
public class TermConsent {

    private String termId;
    private int version;
    private LocalDateTime agreedAt;

    @Builder
    public TermConsent(String termId, int version) {
        this.termId = termId;
        this.version = version;
        this.agreedAt = DateTimeUtil.now();
    }
}
