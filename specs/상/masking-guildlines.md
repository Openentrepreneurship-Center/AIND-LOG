# masking-guildlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전반에서 개인식별정보(PII)를 로그, 응답 DTO, 외부 API 호출 등에 출력할 때 적용해야 하는 마스킹 정책을 정의한다. 마스킹 미적용 상태로 PII가 로그나 응답에 노출되는 것은 보안 사고로 간주하며, PR 단계에서 차단한다.

---

## 2. 원칙

- PII는 기능에 꼭 필요한 최소 범위만 노출한다.
- 마스킹 처리는 `com.backend.global.util.MaskingUtil` 클래스를 통해서만 수행한다. 도메인 코드에 마스킹 문자열 조작 로직을 직접 작성하지 않는다.
- 마스킹 패턴은 필드 유형별로 메서드를 분리하여 일관성을 유지한다.
- 권한에 따라 응답 DTO에 포함되는 마스킹 수준이 달라질 수 있다. 권한 분기는 Service 계층에서 수행한다.
- 로그에서 마스킹 미적용 PII가 발견된 경우 해당 PR은 승인하지 않는다.

---

## 3. 강제 사항

### 3-1. 마스킹 대상 필드

**must**

다음 필드는 로그, 응답 DTO, 외부 API 호출 모두에서 마스킹을 적용한다.

| 필드 | 마스킹 패턴 | 예시 |
|------|------------|------|
| 이메일 | 로컬파트를 `u****`로, 도메인의 첫 글자만 노출 | `user@example.com` → `u****@e***.com` |
| 전화번호 | 가운데 4자리를 `****`로 | `01012345678` → `010-****-5678` |
| 카드번호 | 앞 6자리·뒤 4자리를 제외한 나머지 `****` | `1234-5678-9012-3456` → `****-****-****-3456` |
| 주민번호 | 뒤 7자리 전체 `*******` | `900101-1234567` → `900101-*******` |
| 계좌번호 | 뒤 4자리만 노출, 나머지 `*` | `123-456-789012` → `***-***-****12` |
| 주소 | 도로명 이후(상세주소) `***`로 | `서울시 강남구 테헤란로 123` → `서울시 강남구 테헤란로 ***` |

---

### 3-2. MaskingUtil 클래스

**must**

- 마스킹 유틸은 `com.backend.global.util.MaskingUtil` 클래스에 집중 구현한다.
- 필드 유형별로 메서드를 분리한다. 단일 범용 메서드에 필드 유형을 분기하지 않는다.

  ```java
  // com.backend.global.util.MaskingUtil — 필수 메서드 목록
  public static String maskEmail(String email)
  public static String maskPhone(String phone)
  public static String maskCardNumber(String cardNumber)
  public static String maskRrnBack(String rrn)          // 주민번호 뒤 7자리
  public static String maskAccountNumber(String accountNumber)
  public static String maskAddress(String address)
  ```

- 각 메서드는 `null` 또는 빈 문자열 입력 시 빈 문자열을 반환하며 NullPointerException이 발생하지 않아야 한다.
- 마스킹 결과가 입력 문자열과 동일한 길이를 가지지 않아도 된다. 단, 패턴은 위 표를 준수한다.

---

### 3-3. 로그 마스킹

**must**

- SLF4J를 통해 PII를 로그에 기록할 때는 반드시 `MaskingUtil`을 거친 후 기록한다.

  ```java
  // 금지
  log.info("전화번호 인증 요청: phone={}", phone);

  // 허용
  log.info("전화번호 인증 요청: phone={}", MaskingUtil.maskPhone(phone));
  ```

- 비밀번호, JWT Access/Refresh Token, OTP 코드는 마스킹도 허용하지 않는다. 로그에서 완전히 제외한다.

  ```java
  // 절대 금지 — 마스킹 여부와 무관하게 토큰 값 자체를 로그에 기록하지 않는다
  log.debug("AccessToken: {}", accessToken);
  log.debug("Password: {}", MaskingUtil.maskField(password, 0));
  ```

- `GlobalExceptionHandler`에서 예외 로그를 기록할 때도 파라미터에 PII가 포함되지 않도록 한다.

  ```java
  // com.backend.global.error.GlobalExceptionHandler
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
      ErrorCode errorCode = e.getErrorCode();
      // errorCode.getCode()와 메시지만 기록 — PII 미포함
      log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
      ...
  }
  ```

**should**

- SLF4J MDC에 traceId를 설정하여 요청 단위 로그 추적을 지원한다.
- 구조화 로깅(JSON 포맷)을 사용하는 환경에서는 PII 필드를 별도 마스킹 어댑터로 처리한다.

---

### 3-4. 응답 DTO 마스킹 (권한별 분기)

**must**

- 응답 DTO에서 PII 노출 수준은 호출자의 역할(role)에 따라 분기한다. 분기 로직은 Service 계층에서 처리하고, Controller는 단순 위임한다.

  ```java
  // Service 내 권한 분기 예시 구조
  public UserDetailResponse getUserDetail(String requesterId, String targetUserId) {
      User user = userRepository.findById(targetUserId)
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      boolean isAdmin = /* role 확인 */;
      if (isAdmin) {
          return UserDetailResponse.ofAdmin(user);         // 전체 정보
      }
      return UserDetailResponse.ofUser(user, MaskingUtil.maskPhone(user.getPhone()));
  }
  ```

- 일반 사용자는 자신의 전화번호도 마스킹된 형태로 응답받는다.
- 타 사용자 정보를 조회할 때 이메일·전화번호·주소는 반드시 마스킹한다.

---

### 3-5. 외부 API 호출 마스킹

**must**

- TossPayments, AWS SNS 등 외부 API에 요청을 전송하기 전에 로그 기록 시 PII를 마스킹한다.
- 외부 API 응답에 PII가 포함된 경우, 이를 내부 로그에 그대로 기록하지 않는다.

---

### 3-6. PR 차단 기준

**must**

- PR 코드 리뷰 시 다음 항목이 발견되면 승인을 차단한다.
  - `log.*(...)` 호출에 마스킹 없이 이메일, 전화번호, 카드번호, 주민번호가 포함된 경우
  - 비밀번호, 토큰, OTP 코드가 로그에 포함된 경우 (마스킹 여부 무관)
  - 응답 DTO에 마스킹 없이 PII가 포함된 경우 (권한 검증 없이)
  - `MaskingUtil`을 우회하여 직접 문자열 조작으로 마스킹한 경우

---

## 4. 코드 예시 (decapet 인용)

### ValidationConstants — 전화번호 패턴 참조

```java
// com.backend.global.common.constants.ValidationConstants
public static final String PHONE_REGEX = "^010\\d{8}$";
public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
// 마스킹 전 입력 검증에 활용
```

### MaskingUtil — 전체 구조 (com.backend.global.util.MaskingUtil)

```java
package com.backend.global.util;

public final class MaskingUtil {

    private MaskingUtil() {}

    /** 010-****-5678 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "";
        // 010XXXXXXXX → 010-****-XXXX 형태
        return phone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-****-$3");
    }

    /** u****@e***.com */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "";
        String[] parts = email.split("@");
        String local = parts[0].substring(0, 1) + "****";
        String[] domainParts = parts[1].split("\\.");
        String domain = domainParts[0].substring(0, 1) + "***";
        return local + "@" + domain + "." + domainParts[domainParts.length - 1];
    }

    /** ****-****-****-3456 */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null) return "";
        return cardNumber.replaceAll("\\d{4}-\\d{4}-\\d{4}-(\\d{4})", "****-****-****-$1");
    }

    /** 900101-******* */
    public static String maskRrnBack(String rrn) {
        if (rrn == null) return "";
        return rrn.replaceAll("(\\d{6})-(\\d{7})", "$1-*******");
    }
}
```

### 로그 사용 예시

```java
// 전화번호 인증 서비스 내 로그 — PII 마스킹 후 기록
log.info("SMS 인증 코드 발송: phone={}", MaskingUtil.maskPhone(request.phone()));

// 이메일 중복 확인 — 마스킹 적용
log.info("이메일 중복 확인: email={}", MaskingUtil.maskEmail(email));
```

---

## 5. 체크리스트

### MaskingUtil
- [ ] `com.backend.global.util.MaskingUtil` 클래스에 집중 구현
- [ ] `maskEmail`, `maskPhone`, `maskCardNumber`, `maskRrnBack`, `maskAccountNumber`, `maskAddress` 메서드 분리
- [ ] `null` 입력 시 NPE 없이 빈 문자열 반환

### 로그
- [ ] 이메일·전화번호·카드번호·주민번호 로그 기록 시 MaskingUtil 적용
- [ ] 비밀번호·토큰·OTP 코드 로그에 완전 미포함 (마스킹도 금지)
- [ ] GlobalExceptionHandler 로그에 PII 미포함

### 응답 DTO
- [ ] 역할(role) 기반 마스킹 수준 분기 Service에서 처리
- [ ] 타 사용자 이메일·전화번호·주소 마스킹 적용
- [ ] MaskingUtil 우회 없이 표준 메서드만 사용

### 외부 API
- [ ] 외부 API 요청·응답 로그에 PII 마스킹 적용

### PR 차단
- [ ] 마스킹 미적용 PII 로그 없음 확인
- [ ] 비밀번호·토큰 로그 없음 확인
