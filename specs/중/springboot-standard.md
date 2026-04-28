# springboot-standard.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 Spring Boot 4.0.1 / Java 17 / Gradle 프로젝트로,
레이어드 아키텍처(controller → service → repository), 생성자 주입, JPA + Spring Data,
JWT stateless 인증, springdoc OpenAPI 문서화를 기본 표준으로 사용한다.
이 문서는 기능 추가 시 반드시 준수해야 할 핵심 기술 표준을 정의한다.

---

## 원칙

1. 레이어드 아키텍처를 준수한다. controller 는 service 만, service 는 repository 만 호출한다.
2. 의존성 주입은 생성자 주입만 사용한다.
3. JPA 연관관계 기본 fetch 전략은 `LAZY` 다.
4. 보안 설정은 `SecurityFilterChain` 빈 1개에서 완결한다.
5. 모든 API 에 `@Operation` + `@Tag` 를 붙여 Swagger 문서를 유지한다.

---

## 강제 사항

### must

- 모든 의존성 주입은 생성자 주입으로 선언해야 한다 (`@RequiredArgsConstructor` 또는 명시적 생성자). `@Autowired` 필드 주입 금지.
- JPA `@ManyToOne`, `@OneToOne` 의 fetch 전략은 기본 `LAZY` 이어야 한다.
- `SecurityFilterChain` 빈은 `SecurityConfig` 에 1개만 선언해야 한다.
- CSRF 비활성화, 세션 정책 `STATELESS` 를 설정해야 한다.
- JWT 필터를 `UsernamePasswordAuthenticationFilter` 앞에 등록해야 한다 (`JwtFilter` → `AccountValidationFilter` → `PermissionFilter` 순서).
- 모든 컨트롤러에 `@Tag`, 모든 API 메서드에 `@Operation(summary = "...")` 를 붙여야 한다.
- Swagger 경로(`/docs`, `/swagger-ui/**`, `/api-docs/**`)를 `permitAll()` 로 열어야 한다.
- 빌드 및 실행은 `./gradlew build`, `./gradlew bootRun` 명령으로 처리해야 한다.

### should

- `@Transactional(readOnly = true)` 를 조회 전용 service 메서드에 적용한다.
- `@Transactional` 은 service 레이어에만 선언한다.
- Flyway 마이그레이션으로 스키마를 관리한다.
- 통합 테스트는 Testcontainers PostgreSQL + Flyway 환경에서 수행한다.

---

## 예시 (decapet 인용)

### build.gradle — 주요 의존성 발췌

```groovy
// decapet-official/backend/build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.1'
    id 'io.spring.dependency-management' version '1.1.7'
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
    implementation 'com.bucket4j:bucket4j_jdk17-core:8.14.0'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### SecurityConfig — 핵심 패턴

```java
// com.backend.global.config.SecurityConfig
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AccountValidationFilter accountValidationFilter;
    private final PermissionFilter permissionFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/docs", "/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(accountValidationFilter, JwtFilter.class)
            .addFilterAfter(permissionFilter, AccountValidationFilter.class);

        return http.build();
    }
}
```

### 빌드 및 실행 명령

```bash
./gradlew build
./gradlew bootRun
./gradlew test
```

---

## 체크리스트

- [ ] `@Autowired` 필드 주입이 없다
- [ ] JPA 연관관계 fetch 기본값이 `LAZY` 다
- [ ] `SecurityFilterChain` 빈이 1개뿐이다
- [ ] CSRF 비활성화, 세션 정책 `STATELESS` 가 설정되어 있다
- [ ] JWT 필터가 `UsernamePasswordAuthenticationFilter` 앞에 등록되어 있다
- [ ] 모든 컨트롤러에 `@Tag`, 모든 API 메서드에 `@Operation` 이 있다
- [ ] Swagger 경로가 `permitAll()` 로 열려 있다
- [ ] `./gradlew bootRun` 으로 정상 기동된다
- [ ] Flyway 마이그레이션 스크립트가 버전 순서를 유지한다
