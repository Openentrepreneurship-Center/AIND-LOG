# package_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 루트 패키지 아래의 패키지 구조 규칙을 정의한다.
도메인과 글로벌 인프라를 명확히 분리하고, 도메인 내부 서브패키지를 표준화한다.
규칙을 벗어난 위치에 파일을 생성하지 않는다.

---

## 2. 최상위 패키지 구조 (must)

```
com.backend
├── domain      # 비즈니스 도메인
├── global      # 전역 공통 인프라
└── BackendApplication.java
```

---

## 3. 도메인 패키지 내부 구조 (must)

```
com.backend.domain.{x}
├── controller      # @RestController, *Api 인터페이스
├── service         # @Service
├── repository      # JpaRepository 인터페이스
├── entity          # @Entity, Enum
├── dto
│   ├── request     # 입력 record DTO
│   ├── response    # 출력 record DTO
│   ├── internal    # 계층 간 내부 전달 record
│   └── mapper      # @Component 변환기
├── exception       # BusinessException 하위 예외
└── event           # ApplicationEvent, EventListener
```

```java
// 실제 예시: com.backend.domain.user
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserMapper.java:13
@Component
@RequiredArgsConstructor
public class UserMapper { ... }
```

---

## 4. global 패키지 내부 구조 (must)

```
com.backend.global
├── config      # @Configuration 설정
├── common      # 공통 클래스 (BaseEntity, SuccessResponse, ValidationConstants)
├── error       # GlobalExceptionHandler, ErrorCode, BusinessException
├── security    # JwtProvider, TotpProvider, 진입점 핸들러
├── filter      # Servlet Filter 체인
├── util        # 순수 유틸 클래스
└── service     # 글로벌 공유 서비스 (S3Service 등)
```

---

## 5. 도메인 간 의존 규칙 (must)

- `domain.{x}` → `global.*` 참조 허용
- `global.*` → `domain.*` 참조 금지
- cross-domain 참조는 Service 계층을 통해서만 허용

```java
// 올바른 패턴: UserService에서 auth 도메인 Service 주입
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:40-46
private final TokenService tokenService;
private final SmsService smsService;
```

---

## 6. BaseEntity 상속 (must)

모든 JPA Entity는 `com.backend.global.common.BaseEntity`를 상속한다.

```java
// decapet-official/backend/src/main/java/com/backend/global/common/BaseEntity.java:30
public abstract class BaseEntity implements Persistable<String> {
    @Id
    @Column(length = 26)
    private String id;  // ULID 자동 생성
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
```

---

## 7. 체크리스트

- [ ] 새 클래스가 `com.backend.domain.{x}` 또는 `com.backend.global` 에 올바르게 위치하는가
- [ ] 도메인 내부 서브패키지(controller/service/repository/entity/dto/exception)를 준수하는가
- [ ] `global.*` → `domain.*` 방향의 참조가 없는가
- [ ] cross-domain 참조가 Service 계층을 통해서만 이루어지는가
- [ ] 모든 JPA Entity가 `BaseEntity`를 상속하는가
- [ ] 신규 예외 클래스가 `domain.{x}.exception` 패키지에 위치하는가
