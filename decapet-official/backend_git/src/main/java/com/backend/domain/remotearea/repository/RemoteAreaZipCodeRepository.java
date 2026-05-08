package com.backend.domain.remotearea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.remotearea.entity.RemoteAreaZipCode;

public interface RemoteAreaZipCodeRepository extends JpaRepository<RemoteAreaZipCode, Long> {

    boolean existsByZipCode(String zipCode);
}
