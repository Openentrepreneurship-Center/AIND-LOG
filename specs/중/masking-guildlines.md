# masking-guildlines.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 decapet-official/backend 에서 개인정보(PII) 마스킹 기준을 정의한다.
마스킹은 로그 출력과 응답 DTO 두 곳에서 적용하며, 대상 필드와 포맷을 통일한다.
`com.backend.global.util.MaskingUtil` 클래스를 중앙화된 마스킹 유틸로 운영한다.
해당 클래스가 미존재 시 신설하고, 기존 인라인 마스킹 로직은 이곳으로 이전한다.

---

## 2. 변경/완화 사항

| 상 등급 항목 | 중 등급 변경 내용 |
|---|---|
| `com.poc.backend.util.MaskingUtil` 경로 | `com.backend.global.util.MaskingUtil` 으로 변경 |
| 단일 `maskField(field, maskLength)` API | 필드 유형별 전용 메서드(`maskEmail`, `maskPhone`, `maskCard`) 추가 권장 |
| 마스킹 적용 위치 미명시 | 로그(필수), 응답 DTO(권한별 분기), 외부 API 콜 세 곳 명시 |

---

## 3. 강제 사항

### 3-1. 마스킹 대상 (must)

| 필드 | 마스킹 포맷 예시 |
|---|---|
| 이메일 | `u****@***.com` |
| 전화번호 | `010-****-1234` |
| 카드번호 | `****-****-****-1234` |
| 주민등록번호 | `******-*******` |
| 주소 (상세) | 시/군/구 이하 마스킹 |

### 3-2. 로그 마스킹 (must)

- SLF4J 로깅 시 PII 필드는 반드시 마스킹된 값을 플레이스홀더 `{}` 에 전달한다.
- 객체 전체를 `toString()` 으로 로깅하면서 PII 가 포함되는 구조를 금지한다.
- 비밀번호 · 토큰 · OTP 코드는 마스킹조차 불필요 — 로그에 포함하지 않는다.

```java
// 올바른 예
log.info("전화번호 인증 시도: phone={}", MaskingUtil.maskPhone(request.phone()));

// 금지
log.info("전화번호 인증 시도: {}", request);  // toString() 에 phone 포함 가능
```

### 3-3. 응답 DTO 마스킹 (must)

- 인증된 본인이 조회하는 경우와 관리자가 조회하는 경우를 분리하여 마스킹 여부를 결정한다.
- 본인 조회 응답에도 카드번호 전체를 노출하지 않고 마스킹된 포맷을 기본으로 한다.
- 전체 복호화 값이 필요한 경우 별도의 권한 검증 API 로 분리한다.

### 3-4. 외부 API 콜 (should)

- 외부 시스템(SMS, 이메일 발송 서비스 등)으로 전달하는 로그에는 마스킹된 값을 사용한다.
- 외부 API 요청 · 응답 로깅 시에도 동일한 마스킹 기준을 적용한다.

### 3-5. MaskingUtil 구조 (should)

- `com.backend.global.util.MaskingUtil` 에 static 메서드로 구현한다.
- 메서드는 필드 유형별로 분리한다.

```java
public final class MaskingUtil {
    private MaskingUtil() { }

    public static String maskEmail(String email) { ... }
    public static String maskPhone(String phone) { ... }
    public static String maskCard(String cardNumber) { ... }
    public static String maskField(String field, int prefixLength, int suffixLength) { ... }
    public static boolean isMasked(String field, int prefixLength, int suffixLength) { ... }
}
```

---

## 4. 예시

### 전화번호 마스킹

```java
// 입력: "01012345678"
// 출력: "010-****-5678"
public static String maskPhone(String phone) {
    if (phone == null || phone.length() < 11) return "***";
    return phone.substring(0, 3) + "-****-" + phone.substring(7);
}
```

### 이메일 마스킹

```java
// 입력: "user@example.com"
// 출력: "u***@***.com"
public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "***";
    String[] parts = email.split("@");
    String local = parts[0].substring(0, 1) + "***";
    String[] domain = parts[1].split("\\.");
    return local + "@" + "***" + "." + domain[domain.length - 1];
}
```

### 로그 적용

```java
// PhoneVerifyRequest 처리 서비스
log.info("SMS 인증 요청: phone={}", MaskingUtil.maskPhone(request.phone()));
```

---

## 5. 체크리스트

- [ ] 전화번호 로그 출력 시 `MaskingUtil.maskPhone()` 적용
- [ ] 이메일 로그 출력 시 `MaskingUtil.maskEmail()` 적용
- [ ] 카드번호 로그 출력 시 `MaskingUtil.maskCard()` 적용
- [ ] 비밀번호 · 토큰 · OTP 코드 로그 미출력
- [ ] 응답 DTO 에서 카드번호 전체 노출 없음
- [ ] `MaskingUtil` 이 `com.backend.global.util` 패키지에 위치
- [ ] 객체 `toString()` 로 PII 포함 로깅 없음
- [ ] 외부 API 콜 로그에 마스킹 적용
