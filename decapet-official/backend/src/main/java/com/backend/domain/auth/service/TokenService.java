package com.backend.domain.auth.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.domain.auth.entity.RefreshToken;
import com.backend.domain.auth.entity.TokenBlacklist;
import com.backend.domain.auth.exception.InvalidTempTokenException;
import com.backend.domain.auth.exception.TokenExpiredException;
import com.backend.domain.auth.repository.RefreshTokenRepository;
import com.backend.domain.auth.repository.TokenBlacklistRepository;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.security.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenTtlMs;

    public TokenPairInfo createTokenPair(String userId, String role, List<String> permissions) {
        String accessToken = jwtProvider.createAccessToken(userId, role, permissions);
        String jti = jwtProvider.getJtiFromToken(accessToken);
        Date expiresAt = jwtProvider.getExpirationFromToken(accessToken);

        String refreshToken = jwtProvider.createRefreshToken();
        String refreshTokenHash = jwtProvider.hashToken(refreshToken);

        int ttlDays = (int) (refreshTokenTtlMs / (1000 * 60 * 60 * 24));
        if (ttlDays < 1) ttlDays = 1;

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(refreshTokenHash)
                .ttlDays(ttlDays)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenPairInfo(
                accessToken,
                jti,
                refreshToken,
                expiresAt
        );
    }

    public TokenPairInfo refreshTokens(String refreshToken, String role) {
        if (refreshToken == null) {
            throw new TokenExpiredException();
        }

        String tokenHash = jwtProvider.hashToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(TokenExpiredException::new);

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new TokenExpiredException();
        }

        // 기존 토큰 삭제 후 새 토큰 발급
        String userId = storedToken.getUserId();
        refreshTokenRepository.delete(storedToken);

        List<String> permissions = userRepository.findById(userId)
                .map(User::getPermissions)
                .map(this::toPermissionStrings)
                .orElse(List.of());

        return createTokenPair(userId, role, permissions);
    }

    private List<String> toPermissionStrings(Collection<PermissionType> permissions) {
        return permissions.stream()
                .map(PermissionType::name)
                .toList();
    }

    public void invalidateRefreshToken(String refreshToken) {
        String tokenHash = jwtProvider.hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshTokenRepository::delete);
    }

    public void invalidateAllTokensByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // ===== Temp Token (OTP 인증용) =====

    public String createTempToken(String userId, String purpose) {
        return jwtProvider.createTempToken(userId, purpose);
    }

    public String validateTempTokenAndGetPurpose(String tempToken) {
        if (tempToken == null || !jwtProvider.validateToken(tempToken)) {
            throw new InvalidTempTokenException();
        }
        return jwtProvider.getPurposeFromToken(tempToken);
    }

    public String getUserIdFromTempToken(String tempToken) {
        if (tempToken == null || !jwtProvider.validateToken(tempToken)) {
            throw new InvalidTempTokenException();
        }
        return jwtProvider.getUserIdFromToken(tempToken);
    }

    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || !jwtProvider.validateToken(accessToken)) {
            return;
        }
        String jti = jwtProvider.getJtiFromToken(accessToken);
        if (jti == null) {
            return;
        }
        Date expiration = jwtProvider.getExpirationFromToken(accessToken);
        String userId = jwtProvider.getUserIdFromToken(accessToken);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .expiresAt(expiration.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .build();

        tokenBlacklistRepository.save(blacklist);
    }
}
