package com.backend.domain.banner.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.banner.dto.request.CreateBannerRequest;
import com.backend.domain.banner.entity.Banner;

@Component
public class BannerMapper {

    public Banner toEntity(CreateBannerRequest request, String imageUrl, int displayOrder) {
        return Banner.builder()
                .title(request.title())
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();
    }
}
