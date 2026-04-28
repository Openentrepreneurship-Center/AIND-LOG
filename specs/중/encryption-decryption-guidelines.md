# encryption-decryption-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 비밀번호, PII(전화번호 등), JWT 토큰을 다룰 때 적용해야 하는 암호화·복호화 핵심 정책을 정의한다. 알고리즘 선택과 키 관리의 기본 기준을 제시한다.

---

## 2. 원칙

- 평문 비밀번호를 저장하지 않는다.
- 암호화 키를 코드에 하드코딩하지 않는다.
- 암호화 유틸은 `com.backend.global.util` 패키지에 집중 관리한다.
- 복호화 결과를 응답으로 내보내기 전에 권한을 검증한다.

---

## 3. 강제 사항

### 3-1. 비밀번호

**must**

- 비밀번호는 `BCryptPasswordEncoder`로 단방향 해시한 후 저장한다. `SecurityConfig`에서 Bean으로 등록한다.

  ```java
  // com.backend.global.config.SecurityConfig
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  ```

- 비밀번호를 DB, 로그, 응답 DTO 어디에도 평문으로 저장·출력하지 않는다.
- 비밀번호 검증은 `passwordEncoder.matches(rawPassword, encodedPassword)`만 사용한다.
- 비밀번호 형식은 `ValidationConstants.PASSWORD_REGEX`로 검증한다.

  ```java
  // com.backend.global.common.constants.ValidationConstants
  public static final String PASSWORD_REGEX =
      "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]+$";
  ```

---

### 3-2. PII 암호화

**must**

- PII(전화번호 등)는 AES-256-GCM으로 암호화하여 저장한다.
- 암·복호화는 `com.backend.global.util.AesEncryptor`를 통해서만 수행한다. 도메인 서비스에서 `Cipher`를 직접 초기화하지 않는다.
- 암호화 키는 환경 변수에서 `@Value`로 주입한다. 소스 코드에 하드코딩하지 않는다.

  ```java
  // com.backend.global.util.AesEncryptor
  public AesEncryptor(@Value("${otp.encryption-key}") String encryptionKey) {
      byte[] key = Base64.getDecoder().decode(encryptionKey);
      if (key.length != 32) {
          throw new IllegalArgumentException("암호화 키는 정확히 32바이트여야 합니다.");
      }
      this.keySpec = new SecretKeySpec(key, "AES");
  }
  ```

- IV는 `SecureRandom`으로 매 암호화 시 새로 생성하고, 암호문 앞에 붙여 저장한다.

---

### 3-3. JWT

**must**

- JWT는 JJWT 0.12.x를 사용한다.
- `jwt.secret`은 `application-{profile}.yml`에 정의하고 `@Value("${jwt.secret}")`로 주입한다.
- Access Token과 Refresh Token 만료 시간을 외부 설정 파일에 명시한다.

  ```java
  // com.backend.global.security.JwtProvider
  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration}") long accessExpiration,
      @Value("${jwt.refresh-expiration}") long refreshExpiration) {
      this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
      ...
  }
  ```

- JWT 클레임에 비밀번호, PII, 카드번호를 포함하지 않는다.

---

## 4. 코드 예시 (decapet 인용)

### AesEncryptor — AES-256-GCM 암호화 흐름

```java
// com.backend.global.util.AesEncryptor
private static final String ALGORITHM = "AES/GCM/NoPadding";
private static final int GCM_IV_LENGTH = 12;
private static final int GCM_TAG_LENGTH = 128;

public String encrypt(String plainText) {
    byte[] iv = new byte[GCM_IV_LENGTH];
    random.nextBytes(iv);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
    return Base64.getEncoder().encodeToString(combined);
}
```

---

## 5. 체크리스트

### 비밀번호
- [ ] `BCryptPasswordEncoder` Bean 등록 및 단방향 해시 저장
- [ ] 평문 비밀번호 DB·로그·응답 미포함
- [ ] `passwordEncoder.matches()`로만 검증
- [ ] `ValidationConstants.PASSWORD_REGEX` 형식 검증

### PII 암호화
- [ ] AES-256-GCM 사용
- [ ] `com.backend.global.util.AesEncryptor` 경유
- [ ] 키를 환경 변수 `@Value`로 주입 (하드코딩 없음)
- [ ] IV `SecureRandom` 생성 및 암호문과 함께 저장

### JWT
- [ ] JJWT 0.12.x 사용
- [ ] `jwt.secret` 외부 설정 파일에 정의
- [ ] Access/Refresh 만료 시간 외부 설정
- [ ] 클레임에 비밀번호·PII 미포함
