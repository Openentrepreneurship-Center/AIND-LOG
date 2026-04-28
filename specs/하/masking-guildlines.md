# masking-guildlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 로그를 기록할 때 비밀번호, 토큰, OTP 코드를 절대 출력하지 않아야 하는 최소 마스킹 정책을 정의한다.

---

## 2. 원칙

- 비밀번호, JWT 토큰, OTP 코드는 로그에서 완전히 제외한다.
- 이메일, 전화번호 등 그 외 PII의 마스킹은 이 문서의 의무 적용 범위에서 제외하나, 마스킹 처리를 권장한다.

---

## 3. 강제 사항

**must**

- 비밀번호, JWT Access/Refresh Token, OTP 코드는 어떠한 경우에도 로그에 기록하지 않는다. 마스킹 후 기록도 허용하지 않는다.

  ```java
  // 절대 금지
  log.info("로그인 요청: password={}", password);
  log.debug("발급 토큰: accessToken={}", accessToken);
  log.debug("OTP 코드: code={}", otpCode);

  // 마스킹도 허용하지 않음
  log.debug("password=****");
  log.debug("token={}", accessToken.substring(0, 10) + "...");
  ```

- `GlobalExceptionHandler` 로그에도 비밀번호·토큰이 포함되지 않도록 한다.

  ```java
  // com.backend.global.error.GlobalExceptionHandler
  // e.getMessage()에 비밀번호·토큰이 포함되지 않도록 예외 메시지를 설계한다
  log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
  ```

---

## 4. 코드 예시 (decapet 인용)

```java
// com.backend.global.util.MaskingUtil — 이메일·전화번호 마스킹 권장 사용처
// 비밀번호·토큰은 이 유틸을 통해서도 로그에 기록하지 않는다

// 권장: 전화번호는 마스킹 후 기록
log.info("인증 요청: phone={}", MaskingUtil.maskPhone(phone));

// 금지: 비밀번호는 마스킹 여부와 무관하게 로그 기록 금지
// log.debug("password={}", password);
```

---

## 5. 체크리스트

- [ ] 비밀번호 로그 미기록 (마스킹 포함 금지)
- [ ] JWT Access/Refresh Token 로그 미기록 (마스킹 포함 금지)
- [ ] OTP 코드 로그 미기록 (마스킹 포함 금지)
- [ ] GlobalExceptionHandler 예외 메시지에 비밀번호·토큰 미포함
