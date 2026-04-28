# encryption-decryption-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전반에서 비밀번호, 개인식별정보(PII), JWT 토큰을 다룰 때 적용해야 하는 암호화·복호화 정책을 정의한다. 잘못된 암호화 구현은 데이터 유출 시 피해를 확대하므로, 알고리즘 선택부터 키 관리까지 일관된 기준이 필요하다.

---

## 2. 원칙

- 평문 비밀번호는 어떠한 저장소에도 저장하지 않는다.
- 암호화 키는 코드에 하드코딩하지 않는다. 환경 변수 또는 외부 키 관리 시스템(KMS)에서 주입한다.
- 복호화 결과를 응답으로 내보내기 전에 반드시 권한 검증을 선행한다.
- 암호화 알고리즘은 검증된 표준(BCrypt, AES-256-GCM, HMAC-SHA256)만 사용한다.
- 암호화 유틸 클래스는 `com.backend.global.util` 패키지에 집중 관리한다.

---

## 3. 강제 사항

### 3-1. 비밀번호

**must**

- 비밀번호는 `PasswordEncoder`(BCrypt 구현)로 단방향 해시한 후 저장한다. `SecurityConfig`에서 `BCryptPasswordEncoder`를 Bean으로 등록하여 사용한다.

  ```java
  // com.backend.global.config.SecurityConfig
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  ```

- BCrypt는 내부적으로 salt를 생성하여 해시에 포함한다. salt를 별도 컬럼에 저장할 필요가 없다.
- 평문 비밀번호를 데이터베이스, 로그, 캐시, 응답 DTO 어디에도 저장·출력하지 않는다.
- 비밀번호 검증은 `passwordEncoder.matches(rawPassword, encodedPassword)`로만 수행한다. 직접 비교(`equals`)를 금지한다.
- 비밀번호 형식은 `ValidationConstants.PASSWORD_REGEX`로 검증한다.

  ```java
  // com.backend.global.common.constants.ValidationConstants
  public static final String PASSWORD_REGEX =
      "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]+$";
  // 영문, 숫자, 특수문자를 각각 1자 이상 포함
  ```

**should**

- 비밀번호 변경 시 이전 비밀번호와의 동일 여부를 검사한다.

---

### 3-2. PII (전화번호·주소·주민번호 등)

**must**

- 전화번호, 주소, 주민번호 등 PII는 AES-256-GCM으로 암호화하여 저장한다.
- IV(Initialization Vector)는 암호화마다 `SecureRandom`으로 새로 생성한다. 동일 IV 재사용을 금지한다.
- IV는 암호문 앞에 붙여(prepend) 함께 저장하고, 복호화 시 분리하여 사용한다.

  ```java
  // com.backend.global.util.AesEncryptor — 실제 구현 발췌
  public String encrypt(String plainText) {
      byte[] iv = new byte[GCM_IV_LENGTH];   // 12 bytes
      random.nextBytes(iv);                  // SecureRandom 생성
      // ... IV + 암호문 결합 후 Base64 인코딩
  }

  public String decrypt(String cipherText) {
      byte[] combined = Base64.getDecoder().decode(cipherText);
      byte[] iv = new byte[GCM_IV_LENGTH];
      // combined에서 IV(앞 12바이트)와 암호문을 분리
  }
  ```

- 암호화 키는 반드시 환경 변수 또는 KMS에서 주입한다. `@Value("${otp.encryption-key}")`와 같이 외부화한다. 키를 소스 코드에 문자열 리터럴로 작성하는 것을 절대 금지한다.

  ```java
  // com.backend.global.util.AesEncryptor
  public AesEncryptor(@Value("${otp.encryption-key}") String encryptionKey) {
      byte[] key = Base64.getDecoder().decode(encryptionKey);
      if (key.length != 32) {
          throw new IllegalArgumentException("암호화 키는 정확히 32바이트(256-bit)여야 합니다.");
      }
      this.keySpec = new SecretKeySpec(key, "AES");
  }
  ```

- 키 길이는 32바이트(256비트)여야 한다. 16바이트(128비트) 키를 허용하지 않는다.
- GCM 인증 태그 길이는 128비트(`GCM_TAG_LENGTH = 128`)를 사용한다.
- 암·복호화 유틸은 `com.backend.global.util.AesEncryptor`를 통해서만 사용한다. 각 도메인 서비스에서 `Cipher`를 직접 초기화하지 않는다.
- 키 회전(Key Rotation) 정책을 정의한다. 최소 연 1회 또는 침해 사고 발생 시 키를 교체한다. 키 교체 시 기존 암호화 데이터의 재암호화 절차를 문서화한다.

**should**

- 암호화된 컬럼은 인덱스 대신 해시 인덱스(HMAC-SHA256 등)를 별도 컬럼에 저장하여 검색을 지원한다.

---

### 3-3. JWT

**must**

- JWT는 JJWT 0.12.x 라이브러리를 사용한다. 다른 JWT 라이브러리를 혼용하지 않는다.
- 서명 알고리즘은 HMAC-SHA256(HS256)을 사용한다. 키는 `Keys.hmacShaKeyFor(secret.getBytes())`로 생성한다.
- JWT secret은 `application-{profile}.yml`에 정의하고 `@Value("${jwt.secret}")`로 주입한다. 소스 코드에 문자열로 작성하는 것을 금지한다.

  ```java
  // com.backend.global.security.JwtProvider
  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration}") long accessExpiration,
      @Value("${jwt.refresh-expiration}") long refreshExpiration) {
      this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
      this.accessExpiration = accessExpiration;
      this.refreshExpiration = refreshExpiration;
  }
  ```

- Access Token과 Refresh Token 만료 시간을 `application-{profile}.yml`에 명시적으로 정의한다. 코드 내 하드코딩된 만료 시간을 허용하지 않는다.
- JWT 클레임에는 `userId`, `role`, `permissions`, `jti`(JWT ID)만 포함한다. 비밀번호, PII, 카드번호를 클레임에 포함하는 것을 금지한다.
- 사용된 토큰의 JTI는 블랙리스트 또는 Redis에 기록하여 재사용을 방지한다.

**should**

- Refresh Token은 UUID 형태의 opaque token으로 발급하고, 서버 측 저장소에서 유효성을 관리한다.

---

### 3-4. 복호화 응답 노출

**must**

- 복호화된 PII를 응답 DTO에 포함하기 전에 반드시 소유권 또는 역할 기반 권한 검증을 수행한다.
- 관리자(ADMIN) 권한이 없는 경우 타 사용자의 복호화 데이터를 응답하지 않는다.
- 복호화 실패(`RuntimeException`)는 `BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)`로 래핑하여 반환한다. 스택 트레이스를 응답에 포함하지 않는다.

---

### 3-5. 암호화 유틸 위치

**must**

- 암·복호화 관련 클래스는 `com.backend.global.util` 패키지에 위치한다.
- 도메인 패키지(`com.backend.domain.*`)에 암호화 로직을 직접 구현하지 않는다.
- `AesEncryptor`는 Spring Bean(`@Component`)으로 관리하며, 생성자 주입으로만 사용한다.

---

## 4. 코드 예시 (decapet 인용)

### AesEncryptor — AES-256-GCM 전체 흐름

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
    // IV(12) + 암호문 결합 후 Base64
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
    return Base64.getEncoder().encodeToString(combined);
}
```

### JwtProvider — 토큰 생성

```java
// com.backend.global.security.JwtProvider
public String createAccessToken(String userId, String role, List<String> permissions) {
    String jti = UUID.randomUUID().toString();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessExpiration);
    return Jwts.builder()
            .id(jti)
            .subject(userId)
            .claim("role", role)
            .claim("permissions", permissions)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
}
```

---

## 5. 체크리스트

### 비밀번호
- [ ] `BCryptPasswordEncoder`로 단방향 해시 저장
- [ ] 평문 비밀번호가 DB, 로그, 응답 어디에도 없음
- [ ] `passwordEncoder.matches()`로만 검증
- [ ] `ValidationConstants.PASSWORD_REGEX` 형식 검증 적용

### PII 암호화
- [ ] AES-256-GCM 알고리즘 사용
- [ ] IV를 `SecureRandom`으로 매 암호화 시 신규 생성
- [ ] IV와 암호문을 함께 저장하는 구조
- [ ] 키를 환경 변수에서 `@Value`로 주입 (소스 내 하드코딩 없음)
- [ ] 키 길이 32바이트(256비트) 확인
- [ ] 암·복호화는 `com.backend.global.util.AesEncryptor` 경유
- [ ] 키 회전 정책 정의 및 문서화

### JWT
- [ ] JJWT 0.12.x 사용
- [ ] secret을 `application-{profile}.yml` 외부화
- [ ] Access/Refresh 만료 시간 외부 설정 파일에 정의
- [ ] 클레임에 비밀번호·PII 미포함
- [ ] JTI 기반 재사용 방지 구현

### 복호화 응답
- [ ] 권한 검증 후 복호화 결과 응답
- [ ] 복호화 실패 시 스택 트레이스 응답 미포함
