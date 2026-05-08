package com.backend.domain.sitesetting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.sitesetting.entity.SiteSetting;
import com.backend.domain.sitesetting.exception.SiteSettingNotFoundException;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {

    default SiteSetting findFirstOrThrow() {
        return findAll().stream()
                .findFirst()
                .orElseThrow(SiteSettingNotFoundException::new);
    }

    default Optional<SiteSetting> findFirst() {
        return findAll().stream().findFirst();
    }
}
