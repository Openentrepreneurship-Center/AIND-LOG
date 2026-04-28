# springboot-standard.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 Spring Boot 4.0.1 프로젝트의 최소 필수 설정을 정의한다.
애플리케이션이 정상 기동하고, JWT 인증이 동작하며, Swagger 문서가 활성화되는 것이 기본 목표다.
생성자 주입, JPA 기본 설정, 빌드 명령을 핵심 체크 항목으로 한다.

---

## 2. 변경/완화 사항

| 항목 | 중 등급 원칙 | 하 등급 완화 |
|------|------------|------------|
| 보안 헤더 설정 | CSP, Referrer-Policy 등 상세 헤더 강제 | 기본 CSRF 비활성 + stateless 설정만 필수 |
| 레이트 리밋 | Bucket4j 적용 권장 | 해당 없음 |
| 테스트 | Testcontainers + Flyway 통합 테스트 강제 | 자율 (단, 기본 단위 테스트 권장) |
| `@EnableJpaAuditing` | 설정 클래스에 명시적 선언 강제 | 동일 (완화 없음) |
| 필터 체인 순서 상세 | 4개 필터 순서 정확히 준수 | JwtFilter 위치(UsernamePasswordAuthenticationFilter 앞)만 필수 |

---

## 3. 강제 사항

### must

- `@SpringBootApplication`은 `com.backend` 루트 패키지에 1개만 선언한다.
- `@EnableJpaAuditing`을 반드시 선언하여 `createdAt`, `updatedAt` 자동 설정을 활성화한다.
- 모든 스프링 빈 의존성 주입은 생성자 주입을 사용한다 (`@Autowired` 필드 주입 금지).
- `SecurityFilterChain`에서 CSRF를 비활성화하고 세션 정책을 `STATELESS`로 설정한다.
- `JwtFilter`는 `UsernamePasswordAuthenticationFilter` 앞에 등록한다.
- springdoc Swagger UI 경로(`/swagger-ui/**`, `/api-docs/**`)를 `permitAll()`로 열어둔다.
- 빌드: `./gradlew build` / 테스트: `./gradlew test`

### should

- JPA 연관관계 fetch 전략 기본값을 `LAZY`로 설정한다.
- 모든 엔티티가 `com.backend.global.common.BaseEntity`를 상속하고 ULID PK를 사용하도록 통일한다.

---

## 4. 예시 코드

### 4.1 메인 클래스

```java
// com.backend.BackendApplication
@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

### 4.2 최소 SecurityFilterChain

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 4.3 생성자 주입

```java
// 필드 주입 금지
// @Autowired
// private UserRepository userRepository;  // 금지

// 생성자 주입 (Lombok 활용)
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}
```

---

## 5. 체크리스트

- [ ] `@SpringBootApplication`이 `com.backend` 패키지에 1개만 선언되어 있는가?
- [ ] `@EnableJpaAuditing`이 선언되어 있는가?
- [ ] `@Autowired` 필드 주입이 없고, 생성자 주입만 사용하는가?
- [ ] `SecurityFilterChain`에서 CSRF 비활성 + STATELESS 설정이 적용되어 있는가?
- [ ] `JwtFilter`가 `UsernamePasswordAuthenticationFilter` 앞에 등록되어 있는가?
- [ ] Swagger UI 경로가 `permitAll()`로 열려 있는가?
