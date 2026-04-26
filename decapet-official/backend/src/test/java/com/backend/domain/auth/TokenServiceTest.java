package com.backend.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.domain.auth.entity.RefreshToken;
import com.backend.domain.auth.entity.TokenBlacklist;
import com.backend.domain.auth.exception.InvalidTempTokenException;
import com.backend.domain.auth.exception.TokenExpiredException;
import com.backend.domain.auth.service.TokenService;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

@DisplayName("TokenService 통합 테스트")
class TokenServiceTest extends IntegrationTestBase {

    @Autowired
    private TokenService tokenService;

    @Nested
    @DisplayName("createTokenPair")
    class CreateTokenPair {

        @Test
        @DisplayName("refresh token 해시를 DB에 저장하고, 유효한 access+refresh 토큰을 반환한다")
        void createsAndStoresTokenPair() {
            User user = TestDataFactory.createUser(em);

            TokenPairInfo result = tokenService.createTokenPair(user.getId(), "USER", List.of());

            assertThat(result.accessToken()).isNotBlank();
            assertThat(result.refreshToken()).isNotBlank();
            assertThat(result.accessTokenJti()).isNotBlank();
            assertThat(result.accessTokenExpiresAt()).isNotNull();

            // DB에 refresh token hash 저장 확인
            String hash = jwtProvider.hashToken(result.refreshToken());
            Long count = em.createQuery(
                    "SELECT COUNT(r) FROM RefreshToken r WHERE r.tokenHash = :hash", Long.class)
                    .setParameter("hash", hash)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("refreshTokens")
    class RefreshTokens {

        @Test
        @DisplayName("유효한 refresh token으로 갱신하면 기존 토큰을 삭제하고 새 토큰 쌍을 반환한다")
        void refreshesSuccessfully() {
            User user = TestDataFactory.createUser(em);

            String rawRefreshToken = createRawRefreshToken();
            String hash = jwtProvider.hashToken(rawRefreshToken);

            RefreshToken entity = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenHash(hash)
                    .ttlDays(30)
                    .build();
            em.persist(entity);
            em.flush();

            TokenPairInfo result = tokenService.refreshTokens(rawRefreshToken, "USER");

            assertThat(result.accessToken()).isNotBlank();
            assertThat(result.refreshToken()).isNotBlank();

            // 기존 토큰이 삭제되었는지 확인
            Long oldCount = em.createQuery(
                    "SELECT COUNT(r) FROM RefreshToken r WHERE r.tokenHash = :hash", Long.class)
                    .setParameter("hash", hash)
                    .getSingleResult();
            assertThat(oldCount).isZero();
        }

        @Test
        @DisplayName("만료된 refresh token이면 TokenExpiredException을 던진다")
        void throwsOnExpiredToken() {
            User user = TestDataFactory.createUser(em);

            String rawRefreshToken = createRawRefreshToken();
            String hash = jwtProvider.hashToken(rawRefreshToken);

            RefreshToken entity = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenHash(hash)
                    .ttlDays(1)
                    .build();
            em.persist(entity);
            em.flush();

            // expiresAt을 과거로 강제 변경
            em.createQuery("UPDATE RefreshToken r SET r.expiresAt = :past WHERE r.id = :id")
                    .setParameter("past", java.time.LocalDateTime.now().minusDays(1))
                    .setParameter("id", entity.getId())
                    .executeUpdate();
            em.flush();
            em.clear();

            assertThatThrownBy(() -> tokenService.refreshTokens(rawRefreshToken, "USER"))
                    .isInstanceOf(TokenExpiredException.class);
        }

        @Test
        @DisplayName("null refresh token이면 TokenExpiredException을 던진다")
        void throwsOnNullToken() {
            assertThatThrownBy(() -> tokenService.refreshTokens(null, "USER"))
                    .isInstanceOf(TokenExpiredException.class);
        }
    }

    @Nested
    @DisplayName("blacklistAccessToken")
    class BlacklistAccessToken {

        @Test
        @DisplayName("유효한 access token의 JTI가 token_blacklist 테이블에 저장된다")
        void blacklistsToken() {
            User user = TestDataFactory.createUser(em);

            String accessToken = createRawAccessToken(user);
            String jti = jwtProvider.getJtiFromToken(accessToken);

            tokenService.blacklistAccessToken(accessToken);
            em.flush();

            Long count = em.createQuery(
                    "SELECT COUNT(b) FROM TokenBlacklist b WHERE b.jti = :jti", Long.class)
                    .setParameter("jti", jti)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("null token은 무시된다")
        void ignoresNullToken() {
            tokenService.blacklistAccessToken(null);
            // 예외 없이 정상 종료
        }
    }

    @Nested
    @DisplayName("invalidateAllTokensByUserId")
    class InvalidateAll {

        @Test
        @DisplayName("해당 유저의 모든 refresh token이 삭제된다")
        void deletesAllForUser() {
            User user = TestDataFactory.createUser(em);

            // 2개의 refresh token 생성
            for (int i = 0; i < 2; i++) {
                String raw = createRawRefreshToken();
                RefreshToken entity = RefreshToken.builder()
                        .userId(user.getId())
                        .tokenHash(jwtProvider.hashToken(raw))
                        .ttlDays(30)
                        .build();
                em.persist(entity);
            }
            em.flush();

            tokenService.invalidateAllTokensByUserId(user.getId());
            em.flush();

            Long count = em.createQuery(
                    "SELECT COUNT(r) FROM RefreshToken r WHERE r.userId = :userId", Long.class)
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("createTempToken + validateTempTokenAndGetPurpose")
    class TempToken {

        @Test
        @DisplayName("임시 토큰을 생성하고 검증하면 purpose가 일치한다")
        void roundTrip() {
            User user = TestDataFactory.createUser(em);

            String tempToken = tokenService.createTempToken(user.getId(), "OTP_VERIFY");

            String purpose = tokenService.validateTempTokenAndGetPurpose(tempToken);

            assertThat(purpose).isEqualTo("OTP_VERIFY");
        }

        @Test
        @DisplayName("null 임시 토큰 검증 시 InvalidTempTokenException을 던진다")
        void throwsOnNullTempToken() {
            assertThatThrownBy(() -> tokenService.validateTempTokenAndGetPurpose(null))
                    .isInstanceOf(InvalidTempTokenException.class);
        }
    }
}
