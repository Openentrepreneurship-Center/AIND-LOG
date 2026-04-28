# springboot-standard.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 decapet-official 백엔드의 Spring Boot 4.0.1 개발 표준 핵심을 정의한다.
레이어드 아키텍처(Controller → Service → Repository)를 기반으로 하며, 모든 의존성 주입은 생성자 주입을 사용한다.
JPA + Spring Data를 사용하므로 XML 기반 SQL 매핑은 적용하지 않는다.
보안은 `SecurityFilterChain` + JWT 필터 체인으로 구성하며, API 문서는 springdoc(Swagger)으로 제공한다.
테스트는 Testcontainers + Flyway 조합으로 실제 DB 환경에서 수행한다.

---

## 2. 변경/완화 사항

| 항목 | 상 등급 원칙 | 중 등급 적용 |
|------|------------|------------|
| 데이터 접근 | SQL Mapper (XML) | Spring Data JPA Repository |
| SQL 최적화 | SQL Mapper 배치/N+1 방지 | JPA fetch=LAZY 기본, 필요 시 JPQL/QueryDSL |
| 보안 설정 | 일반 WebSecurityConfigurerAdapter | `SecurityFilterChain` 빈 방식 (`@Configuration` 클래스) |
| 테스트 슬라이스 | `@MybatisTest` | `@DataJpaTest` + Testcontainers |
| 빌드 도구 | Maven 또는 Gradle | Gradle 전용 (`./gradlew`) |

---

## 3. 강제 사항

### 3.1 애플리케이션 구성

**must**

- `@SpringBootApplication`은 최상위 패키지(`com.backend`)에 1개만 선언한다.
- `@EnableJpaAuditing`은 별도 `@Configuration` 클래스 또는 메인 클래스에 선언한다.
- 모든 스프링 빈의 의존성 주입은 생성자 주입만 사용한다 (`@Autowired` 필드 주입 금지).
  Lombok `@RequiredArgsConstructor`를 활용하면 코드를 간결하게 유지할 수 있다.
- `@Value` 또는 `@ConfigurationProperties`를 통해 설정값을 주입한다. 하드코딩 금지.

**should**

- 스케줄러가 필요하면 `@EnableScheduling`을 명시적으로 선언한다.
- 비동기 처리가 필요하면 `@EnableAsync` + `AsyncConfigurer`를 별도 설정 클래스로 분리한다.

### 3.2 JPA 베스트 프랙티스

**must**

- 모든 연관관계 fetch 전략 기본값은 `LAZY`로 설정한다 (`@ManyToOne(fetch = FetchType.LAZY)`).
- `@OneToMany` 컬렉션은 꼭 필요한 경우에만 사용하고, 양방향 연관은 신중하게 결정한다.
- 엔티티에서 직접 setter를 노출하지 않는다. 상태 변경은 의미 있는 도메인 메서드로 캡슐화한다.
- 모든 `@Entity` 클래스는 `com.backend.global.common.BaseEntity`를 상속하고 ULID PK를 사용한다.
- soft-delete 엔티티는 `BaseEntity.delete()`를 호출하여 `deletedAt`을 설정한다.

**should**

- N+1 문제가 의심되면 `@EntityGraph` 또는 fetch join을 적용한다.
- 대량 조회는 페이징(`Pageable`)을 기본으로 한다.

### 3.3 보안

**must**

- `SecurityFilterChain` 빈을 통해 보안 규칙을 설정한다 (`WebSecurityConfigurerAdapter` 사용 금지 — Spring Security 6 이상 제거됨).
- CSRF는 stateless REST API이므로 비활성화한다 (`AbstractHttpConfigurer::disable`).
- 세션 정책은 `STATELESS`로 설정한다.
- JWT 필터 체인 순서를 준수한다:
  1. `HttpsEnforcementFilter`
  2. `JwtFilter`
  3. `AccountValidationFilter`
  4. `PermissionFilter`
- 인증/인가 실패 응답은 `CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`로 처리한다.

**should**

- 레이트 리밋이 필요한 엔드포인트에는 Bucket4j(`com.bucket4j:bucket4j_jdk17-core`)를 적용한다.
- 보안 헤더(CSP, X-Frame-Options, Referrer-Policy 등)는 `SecurityFilterChain`에서 설정한다.

### 3.4 API 문서

**must**

- springdoc(`org.springdoc:springdoc-openapi-starter-webmvc-ui`)을 사용한다.
- 컨트롤러 클래스에 `@Tag`, 메서드에 `@Operation`을 선언한다.
- 스웨거 UI 엔드포인트(`/swagger-ui/**`, `/api-docs/**`)는 `SecurityFilterChain`의 `permitAll()`에 등록한다.

### 3.5 테스트

**must**

- 통합 테스트는 Testcontainers + Flyway 조합으로 실제 PostgreSQL 환경에서 실행한다.
- 공통 통합 테스트 설정은 `IntegrationTestBase` 추상 클래스로 추출하여 중복을 줄인다.

**should**

- 단위 테스트는 Mockito를 사용하여 레이어 격리 후 테스트한다.
- 컨트롤러 테스트는 `@WebMvcTest` 슬라이스를 사용한다.

### 3.6 빌드

**must**

- 빌드: `./gradlew build`
- 테스트: `./gradlew test`
- Gradle wrapper를 통해 실행하며, 전역 설치된 `gradle` 명령을 직접 사용하지 않는다.

---

## 4. 예시 코드

### 4.1 생성자 주입 + @RequiredArgsConstructor

```java
// com.backend.domain.pet.service.PetService
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final UserService userService;

    @Transactional
    public Pet registerPet(String userId, PetCreateRequest request) {
        userService.validateUserExists(userId);
        Pet pet = new Pet(userId, request.getName(), request.getBirthDate());
        return petRepository.save(pet);
    }

    public Pet findPet(String petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
    }
}
```

### 4.2 SecurityFilterChain 구성

```java
// com.backend.global.config.SecurityConfig (실제 코드 발췌)
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/docs", "/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasRole("USER")
                .anyRequest().denyAll()
            )
            .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(accountValidationFilter, JwtFilter.class)
            .addFilterAfter(permissionFilter, AccountValidationFilter.class);

        return http.build();
    }
}
```

### 4.3 BaseEntity + ULID PK

```java
// com.backend.global.common.BaseEntity (실제 코드 발췌)
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

    @Id
    @Column(length = 26)
    private String id;          // ULID

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;  // soft-delete

    protected BaseEntity() {
        this.id = UlidGenerator.generate();
    }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }

    public void delete() {
        this.deletedAt = DateTimeUtil.now();
    }
}
```

### 4.4 springdoc 컨트롤러 선언

```java
@Tag(name = "Pet", description = "반려동물 API")
@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(summary = "반려동물 등록")
    @PostMapping
    public ResponseEntity<PetResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PetCreateRequest request) {
        Pet pet = petService.registerPet(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(PetResponse.from(pet));
    }
}
```

---

## 5. 체크리스트

- [ ] `@SpringBootApplication`이 `com.backend` 패키지에 1개만 선언되어 있는가?
- [ ] `@EnableJpaAuditing`이 설정 클래스 또는 메인 클래스에 선언되어 있는가?
- [ ] 모든 빈 주입이 생성자 주입(`@RequiredArgsConstructor` 포함)으로 이루어지는가?
- [ ] `@Autowired` 필드 주입이 사용된 코드가 없는가?
- [ ] JPA 연관관계 fetch 전략 기본값이 `LAZY`인가?
- [ ] 모든 엔티티가 `BaseEntity`를 상속하고 ULID PK를 사용하는가?
- [ ] `SecurityFilterChain`이 stateless + CSRF 비활성으로 설정되어 있는가?
- [ ] JWT 필터 체인 순서(HttpsEnforcement → Jwt → AccountValidation → Permission)가 유지되는가?
- [ ] springdoc Swagger UI 경로가 `SecurityFilterChain`에서 `permitAll`로 열려 있는가?
- [ ] 통합 테스트가 Testcontainers + Flyway 조합으로 실행되는가?
- [ ] 빌드 및 테스트 명령이 `./gradlew build` / `./gradlew test`인가?
