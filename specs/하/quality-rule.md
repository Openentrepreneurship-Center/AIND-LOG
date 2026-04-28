# quality-rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 Controller, Service, Repository(JPA) 계층 구현 시 반드시 준수해야 하는 최소 보안·품질 규칙을 정의한다.

---

## 2. 원칙

- 외부 입력은 항상 검증한다.
- JPA 파라미터는 반드시 바인딩한다.
- 비밀번호와 토큰은 로그에 기록하지 않는다.

---

## 3. 강제 사항

**must**

- `@RequestBody` DTO에 `@Valid`를 선언한다.

  ```java
  public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) { ... }
  ```

- JPQL 파라미터는 `@Param`으로 바인딩한다. 문자열 연결로 JPQL을 동적 생성하는 것을 금지한다.

  ```java
  // com.backend 도메인 Repository 공통 패턴
  @Query("SELECT u FROM User u WHERE u.phone = :phone")
  Optional<User> findByPhone(@Param("phone") String phone);
  ```

- 목록 조회는 `Pageable`을 적용한다. 무제한 전체 조회를 금지한다.

- 비밀번호와 JWT 토큰은 로그에 기록하지 않는다.

  ```java
  // 절대 금지
  log.info("로그인: password={}", password);
  log.debug("token={}", accessToken);
  ```

---

## 4. 코드 예시 (decapet 인용)

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

---

## 5. 체크리스트

- [ ] `@RequestBody` DTO에 `@Valid` 선언
- [ ] JPQL 파라미터 `@Param` 바인딩 (문자열 연결 JPQL 없음)
- [ ] 목록 조회에 `Pageable` 적용
- [ ] 비밀번호·JWT 토큰 로그 미기록
