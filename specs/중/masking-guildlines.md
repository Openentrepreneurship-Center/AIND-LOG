# masking-guildlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 이메일, 전화번호, 카드번호 등 개인식별정보(PII)를 로그에 기록할 때 적용해야 하는 마스킹 핵심 정책을 정의한다.

---

## 2. 원칙

- PII는 로그에 기록하기 전에 반드시 마스킹한다.
- 마스킹은 `com.backend.global.util.MaskingUtil`을 통해 수행하는 것을 권장한다.
- 비밀번호, 토큰, OTP 코드는 마스킹도 허용하지 않고 로그에서 완전히 제외한다.

---

## 3. 강제 사항

### 3-1. 로그 마스킹 대상

**must**

다음 필드는 로그 기록 시 반드시 마스킹한다.

| 필드 | 마스킹 예시 |
|------|------------|
| 이메일 | `u****@e***.com` |
| 전화번호 | `010-****-5678` |
| 카드번호 | `****-****-****-3456` |

```java
// 금지
log.info("전화번호 인증: phone={}", phone);

// 허용
log.info("전화번호 인증: phone={}", MaskingUtil.maskPhone(phone));
```

**must**

- 비밀번호, JWT 토큰, OTP 코드는 어떠한 경우에도 로그에 기록하지 않는다. 마스킹도 허용하지 않는다.

  ```java
  // 절대 금지 — 마스킹 여부와 무관하게 기록 자체가 금지
  log.debug("password={}", password);
  log.debug("token={}", accessToken);
  log.debug("otp={}", otpCode);
  ```

---

### 3-2. MaskingUtil 사용

**should**

- 마스킹 처리는 `com.backend.global.util.MaskingUtil`의 필드별 메서드를 사용한다.

  ```java
  // com.backend.global.util.MaskingUtil — 권장 메서드
  MaskingUtil.maskEmail(email)
  MaskingUtil.maskPhone(phone)
  MaskingUtil.maskCardNumber(cardNumber)
  ```

- `null` 또는 빈 문자열 입력 시 NPE 없이 빈 문자열을 반환해야 한다.

---

### 3-3. GlobalExceptionHandler 로그

**must**

- 예외 처리 로그에 PII가 포함되지 않도록 한다. 에러 코드와 메시지만 기록한다.

  ```java
  // com.backend.global.error.GlobalExceptionHandler
  log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
  ```

---

## 4. 코드 예시 (decapet 인용)

### ValidationConstants — 마스킹 전 형식 검증

```java
// com.backend.global.common.constants.ValidationConstants
public static final String PHONE_REGEX = "^010\\d{8}$";
// 입력 검증 후 마스킹하여 로그 기록
```

### MaskingUtil 사용 예시

```java
// com.backend.global.util.MaskingUtil 사용
log.info("SMS 인증 요청: phone={}", MaskingUtil.maskPhone(request.phone()));
log.info("이메일 확인: email={}", MaskingUtil.maskEmail(email));
```

---

## 5. 체크리스트

### 로그 마스킹
- [ ] 이메일·전화번호·카드번호 로그 기록 시 MaskingUtil 적용
- [ ] 비밀번호·토큰·OTP 코드 로그에 완전 미포함
- [ ] GlobalExceptionHandler 로그에 PII 미포함

### MaskingUtil
- [ ] `com.backend.global.util.MaskingUtil` 필드별 메서드 사용 권장
- [ ] `null` 입력 시 NPE 없이 빈 문자열 반환
