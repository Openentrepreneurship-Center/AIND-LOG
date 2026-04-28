# encryption-decryption-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 비밀번호와 JWT를 다룰 때 적용해야 하는 최소 암호화 정책을 정의한다.

---

## 2. 원칙

- 평문 비밀번호를 저장하지 않는다.
- JWT secret을 코드에 하드코딩하지 않는다.

---

## 3. 강제 사항

**must**

- 비밀번호는 `BCryptPasswordEncoder`로 단방향 해시한 후 저장한다.

  ```java
  // com.backend.global.config.SecurityConfig
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  ```

- 비밀번호를 DB, 로그, 응답 어디에도 평문으로 출력하지 않는다.
- 비밀번호 검증은 `passwordEncoder.matches(rawPassword, encodedPassword)`만 사용한다.

- JWT secret은 `application-{profile}.yml`에 정의하고 `@Value("${jwt.secret}")`로 주입한다. 소스 코드에 하드코딩하지 않는다.

  ```java
  // com.backend.global.security.JwtProvider
  public JwtProvider(@Value("${jwt.secret}") String secret, ...) {
      this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
  }
  ```

---

## 4. 코드 예시 (decapet 인용)

```java
// com.backend.global.util.AesEncryptor — 암호화 유틸 위치 참고
// AES-256-GCM 기반 암호화. PII 저장 시 이 유틸을 경유한다.
@Component
public class AesEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    // @Value("${otp.encryption-key}")로 키 주입 — 하드코딩 금지
}
```

---

## 5. 체크리스트

- [ ] `BCryptPasswordEncoder` Bean 등록 및 단방향 해시 저장
- [ ] 평문 비밀번호 DB·로그·응답 미포함
- [ ] `passwordEncoder.matches()`로만 검증
- [ ] `jwt.secret`을 `application-{profile}.yml` 외부화 (하드코딩 없음)
