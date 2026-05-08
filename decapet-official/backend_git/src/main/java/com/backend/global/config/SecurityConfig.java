package com.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.backend.global.filter.AccountValidationFilter;
import com.backend.global.filter.HttpsEnforcementFilter;
import com.backend.global.security.CustomAccessDeniedHandler;
import com.backend.global.security.CustomAuthenticationEntryPoint;
import com.backend.global.filter.JwtFilter;
import com.backend.global.filter.PermissionFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AccountValidationFilter accountValidationFilter;
    private final PermissionFilter permissionFilter;
    private final HttpsEnforcementFilter httpsEnforcementFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; frame-ancestors 'none';"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(HeadersConfigurer.XXssConfig::disable) // Modern browsers don't need this
                        .contentTypeOptions(content -> {}) // X-Content-Type-Options: nosniff
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/docs", "/swagger-ui/**", "/api-docs/**").permitAll()

                        // Auth endpoints (no authentication required for login/register)
                        .requestMatchers("/api/v1/auth/sms/**").permitAll()
                        .requestMatchers("/api/v1/auth/password/**").permitAll()
                        .requestMatchers("/api/v1/auth/check-email").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()

                        // Refresh endpoint needs to be accessible with expired access token
                        .requestMatchers("/api/v1/auth/refresh").permitAll()

                        // Webhook endpoints (TossPayments)
                        .requestMatchers("/api/v1/webhooks/**").permitAll()

                        // Admin auth endpoints
                        .requestMatchers("/api/v1/admin/auth/**").permitAll()

                        // Public read endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/breeds/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/terms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicines/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/site-settings").permitAll()

                        // Admin endpoints require ADMIN role
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // User endpoints require USER role
                        .requestMatchers("/api/v1/auth/logout").hasRole("USER")
                        .requestMatchers("/api/v1/users/**").hasRole("USER")
                        .requestMatchers("/api/v1/pets/**").hasRole("USER")
                        .requestMatchers("/api/v1/carts/**").hasRole("USER")
                        .requestMatchers("/api/v1/orders/**").hasRole("USER")
                        .requestMatchers("/api/v1/payments/**").hasRole("USER")
                        .requestMatchers("/api/v1/deliveries/**").hasRole("USER")
                        .requestMatchers("/api/v1/appointments/**").hasRole("USER")
                        .requestMatchers("/api/v1/prescriptions/**").hasRole("USER")
                        .requestMatchers("/api/v1/medicine-carts/**").hasRole("USER")
                        .requestMatchers("/api/v1/custom-products/**").hasRole("USER")
                        .requestMatchers("/api/v1/posts/**").hasRole("USER")
                        .requestMatchers("/api/v1/schedules/**").hasRole("USER")
                        .requestMatchers("/api/v1/remote-area/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicines/**").hasRole("USER")

                        // Deny all other requests (fail-safe)
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // Add HTTPS enforcement filter before everything
                .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(accountValidationFilter, JwtFilter.class)
                .addFilterAfter(permissionFilter, AccountValidationFilter.class);

        return http.build();
    }
}
