package com.backend.support;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.global.security.JwtProvider;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestExternalServiceConfig.class)
public abstract class IntegrationTestBase {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EntityManager em;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Cookie userAccessTokenCookie(User user) {
        List<String> permissions = user.getPermissions().stream()
                .map(PermissionType::name)
                .toList();
        String token = jwtProvider.createAccessToken(user.getId(), "USER", permissions);
        return new Cookie("accessToken", token);
    }

    protected Cookie userAccessTokenCookie(String userId, List<PermissionType> permissions) {
        List<String> permStrings = permissions.stream()
                .map(PermissionType::name)
                .toList();
        String token = jwtProvider.createAccessToken(userId, "USER", permStrings);
        return new Cookie("accessToken", token);
    }

    protected Cookie adminAccessTokenCookie(Admin admin) {
        String token = jwtProvider.createAccessToken(admin.getId(), "ADMIN", List.of());
        return new Cookie("adminAccessToken", token);
    }

    protected String createRawAccessToken(User user) {
        List<String> permissions = user.getPermissions().stream()
                .map(PermissionType::name)
                .toList();
        return jwtProvider.createAccessToken(user.getId(), "USER", permissions);
    }

    protected String createRawRefreshToken() {
        return jwtProvider.createRefreshToken();
    }
}
