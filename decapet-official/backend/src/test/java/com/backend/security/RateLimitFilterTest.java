package com.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.backend.global.config.RateLimitConfig;
import com.backend.global.filter.RateLimitFilter;

@DisplayName("RateLimitFilter 단위 테스트")
class RateLimitFilterTest {

    private RateLimitConfig rateLimitConfig;
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        rateLimitFilter = new RateLimitFilter(rateLimitConfig, true);
    }

    private MockHttpServletRequest createRequest(String path, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        return request;
    }

    @Nested
    @DisplayName("SMS 발송 제한 (10/5분)")
    class SmsSendLimit {

        @Test
        @DisplayName("10회까지 허용, 11회째 429 반환")
        void limitAt10() throws Exception {
            String ip = "10.0.0.1";
            for (int i = 0; i < 10; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/auth/sms/send", ip),
                        response,
                        new MockFilterChain());
                assertThat(response.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/auth/sms/send", ip),
                    blocked,
                    new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);
            assertThat(blocked.getHeader("X-Rate-Limit-Retry-After-Seconds")).isNotNull();
        }
    }

    @Nested
    @DisplayName("SMS 인증 제한 (15/5분)")
    class SmsVerifyLimit {

        @Test
        @DisplayName("15회까지 허용, 16회째 429 반환")
        void limitAt15() throws Exception {
            String ip = "10.0.0.2";
            for (int i = 0; i < 15; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/auth/sms/verify", ip),
                        response,
                        new MockFilterChain());
                assertThat(response.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/auth/sms/verify", ip),
                    blocked,
                    new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("토큰 리프레시 제한 (5/1분)")
    class RefreshLimit {

        @Test
        @DisplayName("5회까지 허용, 6회째 429 반환")
        void limitAt5() throws Exception {
            String ip = "10.0.0.3";
            for (int i = 0; i < 5; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/auth/refresh", ip),
                        response,
                        new MockFilterChain());
                assertThat(response.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/auth/refresh", ip),
                    blocked,
                    new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("관리자 리프레시도 동일한 제한 적용")
        void adminRefreshLimit() throws Exception {
            String ip = "10.0.0.4";
            for (int i = 0; i < 5; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/admin/auth/refresh", ip),
                        response,
                        new MockFilterChain());
                assertThat(response.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/admin/auth/refresh", ip),
                    blocked,
                    new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("인증 엔드포인트 제한 (30/5분)")
    class AuthLimit {

        @Test
        @DisplayName("30회까지 허용, 31회째 429 반환")
        void limitAt30() throws Exception {
            String ip = "10.0.0.5";
            for (int i = 0; i < 30; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/auth/login", ip),
                        response,
                        new MockFilterChain());
                assertThat(response.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/auth/login", ip),
                    blocked,
                    new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("IP 추출")
    class IpExtraction {

        @Test
        @DisplayName("X-Forwarded-For 헤더에서 첫 번째 IP 추출")
        void xForwardedFor() throws Exception {
            // Use different IP via X-Forwarded-For
            MockHttpServletRequest request = createRequest("/api/v1/auth/sms/send", "127.0.0.1");
            request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");

            // Exhaust limit for the forwarded IP
            for (int i = 0; i < 10; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockHttpServletRequest req = createRequest("/api/v1/auth/sms/send", "127.0.0.1");
                req.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
                rateLimitFilter.doFilter(req, response, new MockFilterChain());
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            MockHttpServletRequest blockedReq = createRequest("/api/v1/auth/sms/send", "127.0.0.1");
            blockedReq.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
            rateLimitFilter.doFilter(blockedReq, blocked, new MockFilterChain());
            assertThat(blocked.getStatus()).isEqualTo(429);

            // Different X-Forwarded-For IP should not be blocked
            MockHttpServletResponse notBlocked = new MockHttpServletResponse();
            MockHttpServletRequest differentIp = createRequest("/api/v1/auth/sms/send", "127.0.0.1");
            differentIp.addHeader("X-Forwarded-For", "192.168.1.200");
            rateLimitFilter.doFilter(differentIp, notBlocked, new MockFilterChain());
            assertThat(notBlocked.getStatus()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("429 응답 형식")
    class ResponseFormat {

        @Test
        @DisplayName("429 응답에 올바른 JSON body 포함")
        void responseBody() throws Exception {
            String ip = "10.0.0.99";
            // Exhaust SMS limit
            for (int i = 0; i < 10; i++) {
                rateLimitFilter.doFilter(
                        createRequest("/api/v1/auth/sms/send", ip),
                        new MockHttpServletResponse(),
                        new MockFilterChain());
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            rateLimitFilter.doFilter(
                    createRequest("/api/v1/auth/sms/send", ip),
                    blocked,
                    new MockFilterChain());

            assertThat(blocked.getContentAsString()).contains("\"code\":\"G004\"");
            assertThat(blocked.getContentAsString()).contains("\"success\":false");
            assertThat(blocked.getContentType()).isEqualTo("application/json");
        }
    }
}
