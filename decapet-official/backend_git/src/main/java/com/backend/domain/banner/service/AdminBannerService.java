package com.backend.domain.banner.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.global.common.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.banner.dto.mapper.BannerMapper;
import com.backend.domain.banner.dto.mapper.BannerResponseMapper;
import com.backend.domain.banner.dto.request.CreateBannerRequest;
import com.backend.domain.banner.dto.request.ReorderBannersRequest;
import com.backend.domain.banner.dto.request.UpdateBannerRequest;
import com.backend.domain.banner.dto.response.BannerResponse;
import com.backend.domain.banner.entity.Banner;
import com.backend.domain.banner.exception.BannerNotFoundException;
import com.backend.domain.banner.repository.BannerRepository;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;
import com.backend.global.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminBannerService {

    private static final String BANNER_IMAGE_DIRECTORY = "banners";

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;
    private final BannerResponseMapper bannerResponseMapper;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;

    public BannerResponse createBanner(CreateBannerRequest request, MultipartFile image) {
        validateBannerDateRange(request.startAt(), request.endAt());
        String imageUrl = s3Service.uploadFile(image, BANNER_IMAGE_DIRECTORY);

        try {
            return transactionTemplate.execute(status -> {
                int nextDisplayOrder = bannerRepository.findMaxDisplayOrder().orElse(0) + 1;
                Banner banner = bannerMapper.toEntity(request, imageUrl, nextDisplayOrder);
                Banner savedBanner = bannerRepository.save(banner);
                return bannerResponseMapper.toResponse(savedBanner);
            });
        } catch (Exception e) {
            s3Service.deleteFile(imageUrl);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<BannerResponse> getAllBanners(Pageable pageable) {
        return PageResponse.from(
            bannerRepository.findAll(pageable)
                .map(bannerResponseMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public BannerResponse getBanner(String bannerId) {
        Banner banner = bannerRepository.findByIdOrThrow(bannerId);
        return bannerResponseMapper.toResponse(banner);
    }

    public BannerResponse updateBanner(String bannerId, UpdateBannerRequest request, MultipartFile image) {
        validateBannerDateRange(request.startAt(), request.endAt());
        Banner banner = bannerRepository.findByIdOrThrow(bannerId);
        String oldImageUrl = banner.getImageUrl();

        String newImageUrl = (image != null && !image.isEmpty())
                ? s3Service.uploadFile(image, BANNER_IMAGE_DIRECTORY)
                : null;

        try {
            BannerResponse response = transactionTemplate.execute(status -> {
                String imageUrl = newImageUrl != null ? newImageUrl : oldImageUrl;
                banner.update(
                        request.title(),
                        imageUrl,
                        request.startAt(),
                        request.endAt()
                );
                return bannerResponseMapper.toResponse(banner);
            });

            if (newImageUrl != null && oldImageUrl != null) {
                s3Service.deleteFile(oldImageUrl);
            }
            return response;
        } catch (Exception e) {
            if (newImageUrl != null) {
                s3Service.deleteFile(newImageUrl);
            }
            throw e;
        }
    }

    @Transactional
    public void deleteBanner(String bannerId) {
        Banner banner = bannerRepository.findByIdOrThrow(bannerId);
        String imageUrl = banner.getImageUrl();
        banner.delete();

        if (imageUrl != null) {
            s3Service.deleteFile(imageUrl);
        }
    }

    @Transactional
    public List<BannerResponse> reorderBanners(ReorderBannersRequest request) {
        List<String> bannerIds = request.bannerIds();
        List<Banner> banners = bannerRepository.findAllByIdIn(bannerIds);

        if (banners.size() != bannerIds.size()) {
            throw new BannerNotFoundException();
        }

        long totalActiveBanners = bannerRepository.count();
        if (bannerIds.size() != totalActiveBanners) {
            throw new BusinessException(ErrorCode.BANNER_REORDER_INCOMPLETE);
        }

        Map<String, Banner> bannerMap = banners.stream()
                .collect(Collectors.toMap(Banner::getId, Function.identity()));

        for (int i = 0; i < bannerIds.size(); i++) {
            Banner banner = bannerMap.get(bannerIds.get(i));
            banner.updateDisplayOrder(i + 1);
        }

        return bannerIds.stream()
                .map(bannerMap::get)
                .map(bannerResponseMapper::toResponse)
                .toList();
    }

    private void validateBannerDateRange(java.time.LocalDateTime startAt, java.time.LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCode.INVALID_BANNER_DATE_RANGE);
        }
    }
}
