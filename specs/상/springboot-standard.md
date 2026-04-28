# springboot-standard.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 레이어드 아키텍처(controller → service → repository) 기반의
Spring Boot 4.0.1 / Java 17 / Gradle 프로젝트다.
JPA + Spring Data 로 영속성을 처리하고, JWT 기반 stateless 인증, Bucket4j 레이트 리밋,
springdoc OpenAPI 문서화, Testcontainers + Flyway 기반 통합 테스트를 표준으로 채택한다.
이 문서는 신규 기능 추가 및 코드 리뷰 시 준수해야 할 기술 표준을 정의한다.

---

## 원칙

1. 레이어드 아키텍처를 준수한다. controller 는 service 만, service 는 repository 만 호출한다. 레이어를 건너뛰는 호출 금지.
2. 의존성 주입은 생성자 주입만 사용한다. `@Autowired` 필드 주입 및 setter 주입 금지.
3. JPA 연관관계 기본 fetch 전략은 `LAZY` 다. `EAGER` 사용 시 반드시 팀 리뷰를 거친다.
4. `@OneToMany` 는 성능 문제를 인지하고 신중히 사용한다. 불필요한 컬렉션 로딩을 피한다.
5. 보안 설정은 `SecurityFilterChain` 빈 1개에서 완결한다. 분산 설정 금지.
6. 모든 API 엔드포인트에 `@Operation` + `@Tag` 를 붙여 Swagger 문서를 유지한다.
7. 통합 테스트는 `IntegrationTestBase` 를 상속하고 Testcontainers + Flyway 로 실제 DB 환경에서 검증한다.

---

## 강제 사항

### must (애플리케이션 구조)

- `@SpringBootApplication` 클래스는 프로젝트 전체에 1개만 존재해야 한다.
- JPA Auditing 활성화(`@EnableJpaAuditing`)와 스케줄러 활성화(`@EnableScheduling`)는 진입점 클래스 또는 전용 설정 클래스에 명시해야 한다.
- 모든 빈 의존성은 생성자 주입으로 선언해야 한다 (`@RequiredArgsConstructor` 또는 명시적 생성자).
- 레이어드 아키텍처 규칙을 지켜야 한다. service 가 다른 service 를 호출하는 것은 허용하나, controller 가 repository 를 직접 주입하는 것은 금지.

### must (JPA / 데이터)

- 모든 `@ManyToOne`, `@OneToOne` 의 기본 fetch 전략은 `FetchType.LAZY` 이어야 한다.
- `@OneToMany` 사용 시 해당 컬렉션이 실제로 필요한 컨텍스트인지 검토해야 한다. 단순 ID 조회가 가능하면 컬렉션 매핑 대신 쿼리로 처리한다.
- 엔티티 PK 는 ULID 문자열이어야 한다 (`BaseEntity` 상속 강제).
- Flyway 마이그레이션 스크립트로 스키마를 관리하며, `spring.jpa.hibernate.ddl-auto=validate` 또는 `none` 을 사용한다.

### must (보안)

- `SecurityFilterChain` 빈은 `com.backend.global.config.SecurityConfig` 에서 1개만 선언해야 한다.
- CSRF 는 stateless 정책으로 비활성화해야 한다 (`AbstractHttpConfigurer::disable`).
- 세션 생성 정책은 `SessionCreationPolicy.STATELESS` 이어야 한다.
- JWT 필터 체인 순서를 지켜야 한다: `HttpsEnforcementFilter` → `JwtFilter` → `AccountValidationFilter` → `PermissionFilter`.
- Bucket4j 를 사용하여 공개 엔드포인트 및 인증 엔드포인트에 레이트 리밋을 적용해야 한다. `ErrorCode.TOO_MANY_REQUESTS("G004")` 를 반환해야 한다.
- `anyRequest().denyAll()` 을 마지막 규칙으로 두어 미매핑 경로를 기본 차단해야 한다.

### must (API 문서)

- 모든 컨트롤러에 `@Tag(name = "...")` 를 붙여야 한다.
- 모든 공개 API 메서드에 `@Operation(summary = "...")` 를 붙여야 한다.
- Swagger UI 경로(`/docs`, `/swagger-ui/**`, `/api-docs/**`)는 인증 없이 접근 가능해야 한다.

### must (테스트)

- 통합 테스트는 Testcontainers PostgreSQL + Flyway 를 사용하여 실제 DB 환경에서 수행해야 한다.
- `IntegrationTestBase` 를 상속하여 컨테이너·Flyway 설정을 재사용해야 한다.
- JaCoCo 커버리지 리포트를 생성해야 한다. `./gradlew test jacocoTestReport`.

### should

- 도메인 서비스 단위 테스트는 Mockito 로 repository 를 mock 하여 빠르게 실행한다.
- `@Transactional(readOnly = true)` 를 조회 전용 service 메서드에 적용하여 flush 를 방지한다.
- `@Transactional` 은 service 레이어에만 선언한다. controller 또는 repository 에 선언 금지.

---

## 예시 (decapet 인용)

### build.gradle — 주요 의존성

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

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'

    // OpenAPI
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

    // ULID
    implementation 'com.github.f4b6a3:ulid-creator:5.2.3'

    // Flyway
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
    runtimeOnly 'org.flywaydb:flyway-database-postgresql'

    // Rate Limiting
    implementation 'com.bucket4j:bucket4j_jdk17-core:8.14.0'

    // Testcontainers
    testImplementation platform('org.testcontainers:testcontainers-bom:1.20.4')
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

### SecurityConfig — 필터 체인 1개, JWT 필터 위치

```java
// com.backend.global.config.SecurityConfig
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AccountValidationFilter accountValidationFilter;
    private final PermissionFilter permissionFilter;
    private final HttpsEnforcementFilter httpsEnforcementFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/docs", "/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                // ... 도메인별 권한 설정
                .anyRequest().denyAll()   // 미매핑 경로 기본 차단
            )
            // 필터 순서: Https → Jwt → AccountValidation → Permission
            .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(accountValidationFilter, JwtFilter.class)
            .addFilterAfter(permissionFilter, AccountValidationFilter.class);

        return http.build();
    }
}
```

### 생성자 주입 패턴

```java
// com.backend.domain.pet.service.PetService (예시)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {
    private final PetRepository petRepository;
    private final UserService userService;     // 타 도메인은 service 로만 참조

    @Transactional
    public void registerPet(PetRegisterInfo info) { ... }

    public PetResponse getPet(String petId) { ... }
}
```

### 빌드 및 실행 명령

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test

# 커버리지 리포트
./gradlew test jacocoTestReport
```

---

## 체크리스트

- [ ] `@SpringBootApplication` 클래스가 프로젝트 전체에 1개다
- [ ] `@EnableJpaAuditing` 이 설정 클래스 또는 진입점에 선언되어 있다
- [ ] 모든 의존성 주입이 생성자 주입(`@RequiredArgsConstructor` 또는 명시적 생성자)으로 처리된다
- [ ] `@Autowired` 필드 주입이 없다 (`grep -r "@Autowired" --include="*.java" src/main/` 빈 출력)
- [ ] `SecurityFilterChain` 빈이 `SecurityConfig` 에 1개만 선언되어 있다
- [ ] CSRF 비활성화, 세션 정책 `STATELESS` 가 설정되어 있다
- [ ] JWT 필터 순서 (`HttpsEnforcementFilter` → `JwtFilter` → `AccountValidationFilter` → `PermissionFilter`)가 지켜진다
- [ ] `anyRequest().denyAll()` 이 마지막 규칙이다
- [ ] Bucket4j 레이트 리밋이 적용되어 있다
- [ ] 모든 컨트롤러에 `@Tag`, 모든 API 메서드에 `@Operation` 이 있다
- [ ] Swagger 경로가 `permitAll()` 로 열려 있다
- [ ] JPA 연관관계 fetch 전략이 기본 `LAZY` 다 (`FetchType.EAGER` 잔존 여부 확인)
- [ ] Flyway 마이그레이션 스크립트가 `resources/db/migration/` 에 관리된다
- [ ] 통합 테스트가 Testcontainers + Flyway 기반 `IntegrationTestBase` 를 상속한다
- [ ] `./gradlew test jacocoTestReport` 로 커버리지 리포트가 생성된다
