package com.backend.domain.banner.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.banner.dto.response.BannerResponse;
import com.backend.domain.banner.dto.response.MainBannerResponse;
import com.backend.domain.banner.entity.Banner;

@Component
public class BannerResponseMapper {

    public BannerResponse toResponse(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getDisplayOrder(),
                banner.getStartAt(),
                banner.getEndAt()
        );
    }

    public MainBannerResponse toMainBannerResponse(Banner banner) {
        return new MainBannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getDisplayOrder()
        );
    }
}
