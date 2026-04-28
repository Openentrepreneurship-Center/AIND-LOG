# encryption-decryption-guidelines.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 가장 치명적인 두 가지 암호화 규칙만 강제한다.
비밀번호 평문 저장과 JWT secret 하드코딩은 즉각적인 보안 사고로 이어지므로
규모나 일정에 관계없이 모든 코드에서 반드시 지켜야 한다.

---

## 2. 변경/완화 사항

| 상/중 등급 항목 | 하 등급 변경 내용 |
|---|---|
| AES-GCM PII 암호화 | 권고 (강제 아님) |
| 복호화 응답 권한 검증 | 권고 (강제 아님) |
| 암호화 유틸 위치 규정 | 권고 (강제 아님) |
| 비밀번호 BCrypt | 강제 유지 |
| JWT secret 외부화 | 강제 유지 |

---

## 3. 강제 사항 (2개)

### 규칙 1. 비밀번호 BCrypt (must)

Spring Security `PasswordEncoder` (BCryptPasswordEncoder) 로 해시 후 저장한다.
평문 비밀번호를 DB 에 저장하거나 로그에 출력하는 행위를 절대 금지한다.

```java
// SecurityConfig 에 PasswordEncoder 빈 등록
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 사용 — 평문 비교 금지
passwordEncoder.matches(rawPassword, storedHash);
```

### 규칙 2. JWT secret 외부화 (must)

JWT 서명 비밀키를 코드나 VCS 에 평문으로 커밋하지 않는다.
반드시 `application.yml` 환경변수 참조(`${JWT_SECRET}`) 형태로 관리한다.

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET}
```

---

## 4. 예시

```java
// 잘못된 예 — 절대 금지
private static final String JWT_SECRET = "hardcoded-secret-key-1234";
user.setPassword("plain1234");
```

---

## 5. 체크리스트

- [ ] 비밀번호가 BCrypt 해시로 저장됨 — 평문 없음
- [ ] `passwordEncoder.matches()` 로 비교 — 평문 직접 비교 없음
- [ ] JWT secret 이 코드에 리터럴로 없음
- [ ] `application.yml` 에서 `${...}` 환경변수 참조 확인
