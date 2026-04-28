# masking-guildlines.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 비밀번호 · 토큰 · OTP 코드를 로그에 출력하지 않는 것만 강제한다.
이메일 · 전화번호 · 카드번호 등 기타 PII 마스킹은 권고 사항이며 강제하지 않는다.

---

## 2. 변경/완화 사항

| 상/중 등급 항목 | 하 등급 변경 내용 |
|---|---|
| 이메일 · 전화번호 · 카드번호 로그 마스킹 | 권고 (강제 아님) |
| 응답 DTO 마스킹 | 권고 (강제 아님) |
| `MaskingUtil` 클래스 신설/사용 | 권고 (강제 아님) |
| 외부 API 콜 마스킹 | 권고 (강제 아님) |
| 비밀번호 · 토큰 · OTP 미로깅 | 강제 유지 |

---

## 3. 강제 사항 (1개)

### 규칙 1. 비밀번호 · 토큰 · OTP 미로깅 (must)

비밀번호 · JWT 토큰 · OTP 코드 · refresh token 을 어떤 로그 레벨에서도 출력하지 않는다.
DTO 전체를 `toString()` 으로 로깅할 때 이 필드가 포함되지 않도록 `@ToString.Exclude` 등으로 처리한다.

```java
// 금지
log.debug("OTP 인증: {}", request);          // code 필드 노출
log.info("토큰 발급: token={}", jwtToken);

// 허용
log.info("OTP 인증 시도: phone={}", request.phone());
log.info("토큰 발급 완료: userId={}", userId);
```

---

## 4. 예시

```java
// Lombok 사용 시 민감 필드 제외
@ToString
public class LoginRequest {
    private String email;
    @ToString.Exclude
    private String password;
}
```

---

## 5. 체크리스트

- [ ] 비밀번호 로그 출력 없음
- [ ] JWT 토큰 · refresh token 로그 출력 없음
- [ ] OTP 코드 로그 출력 없음
- [ ] `toString()` 포함 DTO 로깅 시 민감 필드 제외 처리
