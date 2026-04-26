package com.backend.domain.banner.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.banner.dto.mapper.BannerResponseMapper;
import com.backend.domain.banner.dto.response.MainBannerResponse;
import com.backend.domain.banner.repository.BannerRepository;

import lombok.RequiredArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;
    private final BannerResponseMapper bannerResponseMapper;

    public List<MainBannerResponse> getMainBanners() {
        LocalDateTime now = DateTimeUtil.now();

        return bannerRepository.findByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByDisplayOrderAsc(now, now)
                .stream()
                .map(bannerResponseMapper::toMainBannerResponse)
                .toList();
    }
}
