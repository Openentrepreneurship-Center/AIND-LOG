package com.backend.domain.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.admin.exception.AdminNotFoundException;

public interface AdminRepository extends JpaRepository<Admin, String> {

    Optional<Admin> findByLoginId(String loginId);

    default Admin findByLoginIdOrThrow(String loginId) {
        return findByLoginId(loginId)
                .orElseThrow(AdminNotFoundException::new);
    }

    default Admin findByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(AdminNotFoundException::new);
    }
}
