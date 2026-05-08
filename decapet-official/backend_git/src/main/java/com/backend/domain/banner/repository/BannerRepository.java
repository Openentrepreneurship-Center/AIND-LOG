package com.backend.domain.banner.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.backend.domain.banner.entity.Banner;
import com.backend.domain.banner.exception.BannerNotFoundException;

public interface BannerRepository extends JpaRepository<Banner, String> {

    List<Banner> findByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByDisplayOrderAsc(
            LocalDateTime now1, LocalDateTime now2);

    List<Banner> findAllByIdIn(List<String> ids);

    @Query("SELECT MAX(b.displayOrder) FROM Banner b WHERE b.deletedAt IS NULL")
    Optional<Integer> findMaxDisplayOrder();

    default Banner findByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(BannerNotFoundException::new);
    }
}
