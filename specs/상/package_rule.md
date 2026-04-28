# package_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 루트 패키지 아래의 디렉터리·패키지 구조 규칙을 정의한다.
패키지 구조가 곧 아키텍처 경계이므로, 규칙을 벗어난 위치에 파일을 생성하지 않는다.
도메인 간 의존 방향을 단방향으로 강제하여 순환 참조를 예방하고 변경 영향 범위를 최소화한다.

---

## 2. 최상위 패키지 구조 (must)

```
com.backend
├── domain          # 비즈니스 도메인 (각 도메인별 서브패키지)
├── global          # 전역 공통 인프라
└── BackendApplication.java
```

- `domain`: 비즈니스 개념 단위로 분리된 모든 도메인 패키지의 루트
- `global`: 도메인에 속하지 않는 전역 설정, 공통 유틸, 보안, 필터, 에러 처리

---

## 3. 도메인 패키지 내부 구조 (must)

각 도메인 `com.backend.domain.{x}` 아래에 아래 서브패키지를 사용한다.

```
com.backend.domain.{x}
├── controller          # @RestController, *Api 인터페이스
├── service             # @Service 비즈니스 로직
├── repository          # JpaRepository 인터페이스
├── entity              # @Entity JPA 엔티티, Enum
├── dto
│   ├── request         # 입력 record DTO
│   ├── response        # 출력 record DTO
│   ├── internal        # 계층 간 내부 전달 record (예: ProfileUpdateInfo)
│   └── mapper          # @Component 변환기 (예: UserMapper, UserResponseMapper)
├── exception           # BusinessException 하위 예외 클래스
└── event               # ApplicationEvent, EventListener
```

실제 user 도메인 구조:

```
com.backend.domain.user
├── controller  → UserController.java, UserApi.java
├── service     → UserService.java, AdminUserService.java
├── repository  → UserRepository.java, UserTermConsentRepository.java
├── entity      → User.java, PermissionType.java, TermConsent.java
├── dto
│   ├── request → PhoneVerifyRequest.java, UpdateProfileRequest.java, ...
│   ├── response→ UserResponse.java, AdminUserListResponse.java
│   ├── internal→ ProfileUpdateInfo.java
│   └── mapper  → UserMapper.java, UserResponseMapper.java
├── exception   → UserNotFoundException.java, DuplicateEmailException.java, ...
└── event       → UserDeletedEvent.java, UserDeletionEventListener.java
```

---

## 4. global 패키지 내부 구조 (must)

```
com.backend.global
├── config          # @Configuration 설정 클래스
│                   # SecurityConfig, JpaConfig, SwaggerConfig, RateLimitConfig, ...
├── common          # 공통 모델·유틸 클래스
│   ├── BaseEntity.java
│   ├── SuccessResponse.java
│   ├── SuccessCode.java
│   ├── PageResponse.java
│   └── constants
│       └── ValidationConstants.java
├── error           # 에러 처리 인프라
│   ├── GlobalExceptionHandler.java
│   ├── ErrorCode.java (enum)
│   ├── ErrorResponse.java
│   └── exception
│       └── BusinessException.java
├── security        # JWT·TOTP·진입점 핸들러
│   ├── JwtProvider.java
│   ├── TotpProvider.java
│   ├── CustomAuthenticationEntryPoint.java
│   └── CustomAccessDeniedHandler.java
├── filter          # Servlet Filter 체인
│   ├── JwtFilter.java
│   ├── RateLimitFilter.java
│   ├── AccountValidationFilter.java
│   ├── PermissionFilter.java
│   ├── HttpsEnforcementFilter.java
│   └── AbstractPathMatchingFilter.java
├── util            # 순수 유틸 클래스
│   ├── UlidGenerator.java
│   ├── CookieUtil.java
│   ├── DateTimeUtil.java
│   └── AesEncryptor.java
├── service         # 글로벌 공유 서비스
│   └── S3Service.java
└── aop             # AOP (감사 로그 등)
    ├── AdminAudit.java
    └── AdminAuditAspect.java
```

---

## 5. 도메인 간 의존 규칙 (must)

### 5.1 허용 방향
```
domain.{x} → global.*       (도메인이 글로벌 인프라를 참조)
domain.{x}.service → domain.{y}.service   (cross-domain은 Service 호출만)
domain.{x}.service → domain.{y}.repository  (직접 Repository 접근 허용 — 최소화)
```

### 5.2 금지 방향
- `global.*` → `domain.*` 참조 금지 (순환 방지)
- `domain.{x}.controller` → `domain.{y}.*` 직접 참조 금지 — Service를 통해서만 접근
- `domain.{x}.repository` → `domain.{y}.entity` 직접 쿼리 금지 (연관 관계는 JPA 관계 매핑으로만)

### 5.3 cross-domain 호출 패턴
```java
// 올바른 예: UserService가 다른 도메인의 Service를 주입
@Service
@RequiredArgsConstructor
public class UserService {
    private final TokenService tokenService;          // auth 도메인 Service
    private final SmsService smsService;              // auth 도메인 Service
    private final RefreshTokenRepository refreshTokenRepository; // 최소 범위 직접 접근
}
```

---

## 6. BaseEntity 상속 규칙 (must)

- 모든 JPA Entity는 `com.backend.global.common.BaseEntity`를 상속한다.
- `BaseEntity`가 ULID PK 자동 생성, `createdAt`, `updatedAt`, `deletedAt` 감사 필드를 제공한다.
- soft-delete 대상 Entity는 `@SQLRestriction("deleted_at IS NULL")`을 선언해 자동 필터링한다.

```java
// decapet-official/backend/src/main/java/com/backend/global/common/BaseEntity.java:30-58
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

    @Id
    @Column(length = 26)
    private String id;         // ULID 자동 생성

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    protected BaseEntity() {
        this.id = UlidGenerator.generate();
    }

    public void delete() {
        this.deletedAt = DateTimeUtil.now();
    }
}
```

---

## 7. ErrorCode 위치 규칙 (must)

- 모든 에러 코드는 `com.backend.global.error.ErrorCode` enum에서 중앙 관리한다.
- 신규 도메인 기능 추가 시 해당 도메인 섹션에 에러 코드를 추가한다.
- 도메인별 예외 클래스는 `com.backend.domain.{x}.exception` 패키지에 위치하고, `ErrorCode`를 생성자에서 주입한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/exception/UserNotFoundException.java
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

---

## 8. 체크리스트

- [ ] 새 클래스가 `com.backend.domain.{x}` 또는 `com.backend.global` 중 올바른 위치에 있는가
- [ ] 도메인 내부 서브패키지(controller/service/repository/entity/dto/exception/event)를 준수하는가
- [ ] dto 하위에 request/response/internal/mapper 4개 서브패키지를 사용하는가
- [ ] global → domain 방향의 참조가 없는가
- [ ] cross-domain 참조가 Service 계층을 통해서만 이루어지는가
- [ ] 모든 JPA Entity가 `BaseEntity`를 상속하는가
- [ ] soft-delete Entity에 `@SQLRestriction("deleted_at IS NULL")`이 선언되어 있는가
- [ ] 신규 예외 클래스가 `com.backend.domain.{x}.exception` 패키지에 위치하는가
- [ ] 신규 ErrorCode가 `com.backend.global.error.ErrorCode` enum에 추가되었는가
- [ ] 설정 클래스가 `com.backend.global.config`에 위치하는가
