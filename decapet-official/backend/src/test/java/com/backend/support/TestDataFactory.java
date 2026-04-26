package com.backend.support;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;

import jakarta.persistence.EntityManager;

public class TestDataFactory {

    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static final String DEFAULT_PASSWORD = "Password123!";

    public static User createUser(EntityManager em) {
        int n = counter.getAndIncrement();
        User user = User.builder()
                .email("testuser" + n + "@test.com")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .phone("010" + String.format("%08d", n))
                .name("테스트" + n)
                .zipCode("12345")
                .address("서울시 강남구 테스트로 " + n)
                .build();
        em.persist(user);
        em.flush();
        return user;
    }

    public static User createUserWithPermissions(EntityManager em, Set<PermissionType> permissions) {
        User user = createUser(em);
        user.updatePermissions(permissions);
        em.flush();
        return user;
    }

    public static User createUserWithAllPermissions(EntityManager em) {
        return createUserWithPermissions(em, Set.of(
                PermissionType.PRODUCT_PURCHASE,
                PermissionType.APPOINTMENT_BOOKING,
                PermissionType.INFORMATION_SHARING,
                PermissionType.HOSPITAL_SEARCH,
                PermissionType.REPORT_CENTER
        ));
    }

    public static Admin createAdmin(EntityManager em) {
        int n = counter.getAndIncrement();
        Admin admin = Admin.builder()
                .loginId("admin" + n)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .build();
        em.persist(admin);
        em.flush();
        return admin;
    }
}
