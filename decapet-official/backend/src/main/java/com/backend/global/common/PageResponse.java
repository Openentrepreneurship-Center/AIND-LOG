package com.backend.global.common;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PageableInfo pageable;
    private final long totalElements;
    private final int totalPages;

    @Builder
    private PageResponse(List<T> content, PageableInfo pageable, long totalElements, int totalPages) {
        this.content = content;
        this.pageable = pageable;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageable(PageableInfo.builder()
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .build())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Getter
    @Builder
    public static class PageableInfo {
        private final int pageNumber;
        private final int pageSize;
    }
}
