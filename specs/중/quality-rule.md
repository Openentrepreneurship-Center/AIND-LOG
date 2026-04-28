# quality-rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 Controller, Service, Repository(JPA) 계층을 구현할 때 준수해야 하는 핵심 보안·품질 규칙을 정의한다. 입력 검증, 인증, 트랜잭션, 데이터 접근, 응답 포맷, 로깅 보안을 다룬다.

---

## 2. 원칙

- 외부 입력은 항상 검증한다.
- 인증 컨텍스트는 Controller에서 Service로 명시적으로 전달한다.
- 응답은 `SuccessResponse`로 일원화한다.
- 엔티티를 직접 노출하지 않고 DTO로 변환 후 반환한다.
- 비밀번호와 토큰은 로그에 절대 기록하지 않는다.

---

## 3. 강제 사항

### 3-1. Controller

**must**

- `@RequestBody` DTO에 `@Valid`를 선언한다.

  ```java
  public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) { ... }
  ```

- 인증이 필요한 엔드포인트는 `@AuthenticationPrincipal String userId`를 사용하여 사용자 범위를 고정한다.

- 모든 정상 응답은 `SuccessResponse.of(SuccessCode, data)` 형태로 반환한다.

  ```java
  return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS, responseDto));
  ```

- 비밀번호와 JWT 토큰은 로그에 기록하지 않는다.

**should**

- PathVariable, RequestParam에도 `ValidationConstants`의 정규식을 적용한다.

---

### 3-2. Service

**must**

- 쓰기 메서드에 `@Transactional(rollbackFor = Exception.class)`을 선언한다.
- 조회 메서드에 `@Transactional(readOnly = true)`를 선언한다.
- 도메인 검증(존재/상태/중복/소유권)을 Service에서 수행한다. `BusinessException(ErrorCode)` 형태로 예외를 발생시킨다.

  ```java
  User user = userRepository.findById(userId)
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  ```

- 비밀번호와 토큰은 로그에 기록하지 않는다.

**should**

- 감사 로그는 `Propagation.REQUIRES_NEW`로 별도 트랜잭션에서 처리한다.

---

### 3-3. Repository(JPA)

**must**

- JPQL 파라미터는 `@Param`으로 바인딩한다. 문자열 연결로 JPQL을 동적 생성하는 것을 금지한다.

  ```java
  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);
  ```

- 목록 조회는 `Pageable`을 사용한다.
- Soft Delete 엔티티에 `@SQLRestriction`을 선언한다.

**should**

- 동적 검색 조건은 `Specification<T>`을 사용한다.

---

### 3-4. 응답

**must**

- 엔티티를 Controller 응답으로 직접 반환하지 않는다. DTO로 변환 후 반환한다.
- 에러 응답은 `GlobalExceptionHandler`가 `ErrorResponse`로 처리한다.

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

### GlobalExceptionHandler — 예외 처리

```java
// com.backend.global.error.GlobalExceptionHandler
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
    Map<String, String> fieldErrors = extractFieldErrors(e);
    ErrorResponse response = new ErrorResponse(errorCode, fieldErrors);
    return ResponseEntity.status(errorCode.getStatus()).body(response);
}
```

---

## 5. 체크리스트

### Controller
- [ ] `@RequestBody` DTO에 `@Valid` 선언
- [ ] `@AuthenticationPrincipal String userId` 사용
- [ ] 응답이 `SuccessResponse.of(...)` 형태
- [ ] 비밀번호·토큰 로그 미기록

### Service
- [ ] `@Transactional(rollbackFor = Exception.class)` 선언
- [ ] `@Transactional(readOnly = true)` 선언 (조회)
- [ ] 도메인 검증 수행 및 `BusinessException(ErrorCode)` 사용
- [ ] 비밀번호·토큰 로그 미기록

### Repository
- [ ] JPQL 파라미터 `@Param` 바인딩
- [ ] 목록 조회에 `Pageable` 적용
- [ ] Soft Delete 엔티티에 `@SQLRestriction` 선언

### 응답
- [ ] 엔티티 직접 반환 없음
- [ ] 에러 응답 `GlobalExceptionHandler` 경유
