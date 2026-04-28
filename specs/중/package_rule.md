# package_rule.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend의 패키지 구조 규칙을 정의한다.
상 등급 원본(`package_rule.md`)은 내용이 없었으므로, 본 문서가 실질적인 기준 역할을 한다.
패키지 루트는 `com.backend`이며 도메인 기반 구조를 사용한다.
cross-domain 참조는 service 계층 호출로만 허용하고, repository 직접 참조는 원칙적으로 금지한다.

## 변경/완화 사항

- 상 등급 원본 미정의 → 본 등급에서 전체 패키지 트리를 새로 정의
- 상 등급의 `com.poc.backend` 패키지 기준 → `com.backend`로 변경
- MyBatis XML Mapper 경로(`resources/mybatis/mapper/`) → 해당 없음 (JPA 사용)

## 이 등급에서 강제하는 것

**must**
- 모든 소스코드는 `com.backend` 하위에 위치한다.
- 도메인 코드는 `com.backend.domain.{domain}` 하위에 위치한다.
- 글로벌 공통 코드는 `com.backend.global` 하위에 위치한다.
- 각 도메인 패키지는 아래 서브패키지 구조를 따른다.
- cross-domain 의존성은 service 계층 메서드 호출로만 허용한다. 다른 도메인의 repository를 직접 주입하지 않는다.

**should**
- 도메인 내부에서만 사용하는 예외는 `{domain}.exception` 패키지에 위치시킨다.
- 도메인 간 이벤트는 `{domain}.event` 패키지에 위치시킨다.
- DTO 변환 컴포넌트는 `{domain}.dto.mapper` 패키지에 위치시킨다.

## 패키지 트리

```
com.backend
├── domain
│   └── {domain}                  예: user, auth, pet, product, order
│       ├── controller            @RestController, API 인터페이스
│       ├── service               @Service, 비즈니스 로직
│       ├── repository            JpaRepository / JpaSpecificationExecutor 확장
│       ├── entity                @Entity, @MappedSuperclass
│       ├── dto
│       │   ├── request           record (입력 DTO)
│       │   ├── response          record (출력 DTO)
│       │   ├── internal          도메인 내부 전달 객체
│       │   └── mapper            @Component Entity↔DTO 변환 클래스
│       ├── exception             도메인 전용 BusinessException 하위 클래스
│       └── event                 ApplicationEvent 하위 클래스
└── global
    ├── config                    @Configuration 클래스 (Security, JPA Auditing 등)
    ├── common                    BaseEntity, SuccessResponse, SuccessCode
    │   └── constants             ValidationConstants 등 공통 상수
    ├── error                     GlobalExceptionHandler, ErrorCode, ErrorResponse, BusinessException
    ├── security                  JWT 관련 필터·Provider·UserDetails
    ├── filter                    Servlet Filter (레이트리밋 등)
    └── util                      UlidGenerator, CookieUtil, DateTimeUtil 등
```

## 예시 코드

cross-domain 참조 올바른 예 (UserService에서 auth 도메인 service 호출):

```java
// com.backend.domain.user.service.UserService
@Service
@RequiredArgsConstructor
public class UserService {
    // 같은 도메인 repository는 직접 주입 허용
    private final UserRepository userRepository;

    // 다른 도메인은 service를 통해서만 참조
    private final TokenService tokenService;   // com.backend.domain.auth.service.TokenService
    private final SmsService smsService;       // com.backend.domain.auth.service.SmsService
}
```

cross-domain 참조 잘못된 예:

```java
// 금지: 다른 도메인의 repository를 직접 주입
@Service
@RequiredArgsConstructor
public class UserService {
    private final RefreshTokenRepository refreshTokenRepository; // auth 도메인 repo 직접 주입은 피한다
}
```

global 패키지 구조 예시 (global/error/ErrorCode.java, global/common/BaseEntity.java):

```java
// com.backend.global.error.ErrorCode
@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G001", "요청 값이 유효하지 않습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}

// com.backend.global.common.BaseEntity
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {
    @Id
    @Column(length = 26)
    private String id;  // ULID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
```

## 체크리스트

- [ ] 모든 소스가 `com.backend` 루트 하위에 위치하는가?
- [ ] 도메인 코드가 `com.backend.domain.{domain}` 하위에 위치하는가?
- [ ] 글로벌 공통 코드가 `com.backend.global` 하위에 위치하는가?
- [ ] 다른 도메인의 repository를 직접 주입하지 않고 service를 통해 참조하는가?
- [ ] 도메인 전용 예외가 `{domain}.exception` 패키지에 위치하는가?
- [ ] DTO가 `dto.request` / `dto.response` / `dto.internal` / `dto.mapper`로 분리되어 있는가?
- [ ] 글로벌 설정(`@Configuration`)이 `global.config` 패키지에 위치하는가?
- [ ] 공통 상수(`ValidationConstants`)가 `global.common.constants` 패키지에 위치하는가?
