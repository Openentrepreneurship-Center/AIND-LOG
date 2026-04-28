# quality-rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전반에서 Controller, Service, Repository(JPA) 계층 구현 시 반드시 준수해야 하는 보안·품질 규칙을 정의한다. 입력 검증, 인증/인가, 트랜잭션 정책, 데이터 접근 안전성, 응답 포맷, 로깅 보안을 포함한다.

---

## 2. 원칙

- 모든 외부 입력은 신뢰하지 않는다. 계층을 가로지르더라도 재검증한다.
- 인증 컨텍스트는 Controller에서 Service로 명시적으로 전달한다. Service는 SecurityContext를 직접 참조하지 않는다.
- 응답 포맷은 `SuccessResponse`로 일원화하여 클라이언트와의 계약을 유지한다.
- 엔티티는 절대 직접 노출하지 않는다. DTO로 변환 후 반환한다.
- 로그에 비밀번호, 토큰, 주민번호, 카드번호가 포함되는 순간 보안 사고로 간주한다.

---

## 3. 강제 사항

### 3-1. Controller 계층

**must**

- `@RequestBody` DTO 파라미터에 `@Valid`를 반드시 선언한다.

  ```java
  public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) { ... }
  ```

- 인증이 필요한 엔드포인트는 `@AuthenticationPrincipal String userId`로 사용자 범위를 고정한다. Service에 `userId`를 명시적으로 전달하여 타 사용자 데이터 접근을 차단한다.

- 모든 정상 응답은 `SuccessResponse.of(SuccessCode)` 또는 `SuccessResponse.of(SuccessCode, data)` 형태로 반환한다. 커스텀 Map이나 원시 타입을 직접 반환하는 것을 금지한다.

- PathVariable과 RequestParam에도 정규식 검증을 적용한다. `ValidationConstants`에 정의된 상수를 재사용한다.

  ```java
  // ValidationConstants.PHONE_REGEX = "^010\\d{8}$"
  @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
  @RequestParam String phone
  ```

- 민감정보(이메일, 전화번호, 카드번호)를 로그에 기록할 때는 반드시 마스킹 처리 후 기록한다. 비밀번호와 토큰은 어떠한 경우에도 로그에 기록하지 않는다.

**should**

- Swagger(`@Operation`, `@ApiResponse`) 어노테이션을 통해 API 스펙을 명시한다.
- 요청/응답 클래스에 `@Schema`를 선언하여 문서 자동화를 지원한다.

---

### 3-2. Service 계층

**must**

- 모든 쓰기 메서드에 `@Transactional(rollbackFor = Exception.class)`을 선언한다. Checked Exception도 롤백 대상에 포함한다.
- 조회 전용 메서드에는 `@Transactional(readOnly = true)`를 적용한다.
- 감사 로그 기록은 `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 별도 트랜잭션에서 수행한다. 메인 트랜잭션 롤백 시에도 감사 기록이 보존되어야 한다.
- 도메인 핵심 검증(존재 여부, 상태, 중복, 한도)을 Service 내에서 수행한다. Controller에 검증 책임을 위임하지 않는다.

  | 검증 유형 | 예시 ErrorCode |
  |-----------|----------------|
  | 존재 여부 | `USER_NOT_FOUND`, `PET_NOT_FOUND`, `ORDER_NOT_FOUND` |
  | 상태 검증 | `INVALID_ORDER_STATUS`, `APPOINTMENT_ALREADY_COMPLETED` |
  | 중복 검증 | `DUPLICATE_EMAIL`, `DUPLICATE_PHONE`, `DUPLICATE_TRACKING_NUMBER` |
  | 한도 검증 | `INSUFFICIENT_STOCK`, `WEIGHT_UPDATE_RESTRICTED` |

- 사용자 소유권 검증(ownership check)을 반드시 수행한다. `userId`와 도메인 엔티티의 owner가 일치하는지 확인 후 처리한다.
- 커스텀 예외는 `BusinessException(ErrorCode)` 형태로 발생시킨다. `GlobalExceptionHandler`가 이를 포착하여 `ErrorResponse`로 변환한다.
- 비밀번호와 토큰은 어떠한 상황에서도 로그에 기록하지 않는다.

**should**

- 복잡한 도메인 로직은 엔티티 내부 메서드로 위임하여 Service를 얇게 유지한다.

---

### 3-3. Repository(JPA) 계층

**must**

- JPQL 또는 `@Query`에서 모든 입력값은 `@Param`으로 바인딩한다. 문자열 연결(String concatenation)로 JPQL을 동적 생성하는 것을 절대 금지한다.

  ```java
  // 올바른 예
  @Query("SELECT u FROM User u WHERE u.phone = :phone")
  Optional<User> findByPhone(@Param("phone") String phone);

  // 금지
  @Query("SELECT u FROM User u WHERE u.phone = '" + phone + "'")  // 절대 금지
  ```

- 동적 검색 조건이 필요한 경우 `Specification<T>`을 사용한다. 조건 분기를 JPQL 문자열로 처리하지 않는다.
- 목록 조회는 반드시 `Pageable`을 사용한다. 무제한 전체 조회(`findAll()` 무 파라미터)를 운영 코드에서 금지한다.
- Soft Delete 엔티티에는 `@SQLRestriction("deleted_at IS NULL")` 또는 `@SQLRestriction("deleted = false")`를 엔티티에 선언한다. 삭제 플래그와 삭제 일시를 `BaseEntity`의 `deletedAt` 필드로 관리한다.

  ```java
  // BaseEntity.delete() 호출 시 deletedAt이 자동 설정됨
  public void delete() {
      this.deletedAt = DateTimeUtil.now();
  }
  ```

- SELECT 컬럼을 명시하는 Projection을 활용하여 불필요한 민감 컬럼 노출을 방지한다.

**should**

- N+1 문제가 예상되는 연관 조회는 `@EntityGraph` 또는 `JOIN FETCH`로 해결한다.
- 읽기 전용 레포지토리 메서드는 `@Transactional(readOnly = true)` 서비스 트랜잭션 내에서 호출한다.

---

### 3-4. 응답 포맷

**must**

- 엔티티(`@Entity` 클래스)를 Controller 응답으로 직접 반환하는 것을 금지한다. DTO 클래스(record 또는 class)로 변환 후 반환한다.
- 정상 응답은 아래 형태를 준수한다.

  ```java
  return ResponseEntity
      .status(HttpStatus.OK)
      .body(SuccessResponse.of(SuccessCode.SUCCESS, responseDto));
  ```

- 에러 응답은 `GlobalExceptionHandler`가 `ErrorResponse`로 일원화하여 반환한다. Controller에서 직접 에러 JSON을 구성하지 않는다.

---

### 3-5. Soft Delete

**must**

- 데이터를 물리 삭제하지 않는다. `BaseEntity.delete()`를 호출하여 `deletedAt`을 기록한다.
- `@SQLRestriction`을 엔티티에 선언하여 삭제된 레코드가 일반 조회에서 자동 제외되도록 한다.
- 삭제 API의 Service 메서드는 삭제 플래그 설정 전 소유권 검증을 선행한다.

---

### 3-6. 로깅 보안

**must**

- 다음 데이터는 로그에 절대 기록하지 않는다.
  - 비밀번호 (평문·해시 모두 포함)
  - JWT Access Token / Refresh Token
  - 주민번호
  - 카드번호 (마스킹되지 않은 전체 번호)
  - OTP 코드

- 이메일, 전화번호 등 PII는 마스킹 후 기록한다.

  ```java
  // 금지
  log.info("사용자 로그인: email={}, password={}", email, password);

  // 허용
  log.info("사용자 로그인 시도: email={}", maskEmail(email));
  ```

- `GlobalExceptionHandler`의 `handleException`은 스택 트레이스를 로그에 기록하되(`log.error`), 클라이언트 응답에는 포함하지 않는다.

**should**

- SLF4J MDC를 활용하여 요청별 추적 ID(traceId)를 로그에 포함한다.

---

## 4. 코드 예시 (decapet 인용)

### PhoneVerifyRequest — 입력 검증

```java
// com.backend.domain.user.dto.request.PhoneVerifyRequest
public record PhoneVerifyRequest(
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
    String phone,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = ValidationConstants.OTP_CODE_REGEX, message = ValidationConstants.OTP_CODE_MESSAGE)
    String code
) {}
```

### ErrorCode — 도메인별 에러 코드

```java
// com.backend.global.error.ErrorCode (일부)
DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 등록된 이메일입니다."),
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PR002", "재고가 부족합니다."),
```

### GlobalExceptionHandler — 글로벌 예외 처리

```java
// com.backend.global.error.GlobalExceptionHandler
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
    ErrorResponse response = new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
}
```

### SecurityConfig — 인가 설정

```java
// com.backend.global.config.SecurityConfig
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/users/**").hasRole("USER")
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().denyAll()  // fail-safe: 미정의 경로 전체 차단
)
```

---

## 5. 체크리스트

### Controller
- [ ] 모든 `@RequestBody` DTO에 `@Valid` 선언
- [ ] 인증 필요 엔드포인트에 `@AuthenticationPrincipal String userId` 사용
- [ ] 응답이 `SuccessResponse.of(...)` 형태인지 확인
- [ ] PathVariable/RequestParam에 정규식 검증 적용 (`ValidationConstants` 참조)
- [ ] 로그에 비밀번호·토큰 기록 없음
- [ ] 로그에 이메일·전화번호 등 PII 마스킹 적용

### Service
- [ ] 쓰기 메서드에 `@Transactional(rollbackFor = Exception.class)` 선언
- [ ] 조회 메서드에 `@Transactional(readOnly = true)` 선언
- [ ] 감사 로그는 `Propagation.REQUIRES_NEW` 트랜잭션 분리
- [ ] 도메인 검증(존재/상태/중복/한도/소유권) 수행
- [ ] `BusinessException(ErrorCode)` 형태로 예외 발생
- [ ] 비밀번호·토큰 로그 기록 없음

### Repository(JPA)
- [ ] JPQL 파라미터 전부 `@Param` 바인딩
- [ ] 문자열 연결 JPQL 동적 생성 없음
- [ ] 동적 검색은 `Specification<T>` 사용
- [ ] 목록 조회에 `Pageable` 적용
- [ ] Soft Delete 엔티티에 `@SQLRestriction` 선언

### 응답
- [ ] 엔티티 직접 반환 없음 (DTO 변환 후 반환)
- [ ] 에러 응답은 `GlobalExceptionHandler` 경유
