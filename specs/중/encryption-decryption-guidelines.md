# encryption-decryption-guidelines.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 decapet-official/backend 의 암호화 · 복호화 구현 기준을 정의한다.
비밀번호는 BCrypt 단방향 해시로, OTP secret 등 PII 는 AES-256-GCM 양방향 암호화로 처리한다.
암호화 키는 환경변수 또는 외부 설정으로 관리하며 코드에 하드코딩하지 않는다.
JWT 서명 비밀키도 동일 원칙을 적용한다.

---

## 2. 변경/완화 사항

| 상 등급 항목 | 중 등급 변경 내용 |
|---|---|
| `com.poc.backend.util.AesEncrtpyUtil` 경로 | `com.backend.global.util.AesEncryptor` (기존 구현 그대로 사용) |
| 알고리즘 미명시 | AES/GCM/NoPadding 256-bit, IV 12 byte, Tag 128 bit 으로 명시 |
| 키 관리 방법 미명시 | `application.yml` `${otp.encryption-key}` 외부화, 32 byte Base64 인코딩 |

---

## 3. 강제 사항

### 3-1. 비밀번호 (must)

- Spring Security `PasswordEncoder` (BCryptPasswordEncoder) 로 단방향 해시 후 저장한다.
- 평문 비밀번호를 DB 에 저장하거나 로그에 출력하는 것을 절대 금지한다.
- 비밀번호 검증은 `passwordEncoder.matches(raw, encoded)` 를 사용하고, 평문을 직접 비교하지 않는다.

```java
// 올바른 예
String hashed = passwordEncoder.encode(rawPassword);
user.updatePassword(hashed);
```

### 3-2. PII 양방향 암호화 (must)

- OTP secret 등 복호화가 필요한 PII 는 `com.backend.global.util.AesEncryptor` 를 사용한다.
- `AesEncryptor` 는 AES/GCM/NoPadding, 256-bit 키, 12-byte IV, 128-bit 인증 태그를 사용한다.
- IV 는 암호화마다 `SecureRandom` 으로 새로 생성하며, 암호문 앞에 prepend 하여 저장한다.
- 암호화 키는 `@Value("${otp.encryption-key}")` 로 주입하고, 코드나 VCS 에 평문 키를 커밋하지 않는다.
- 키 길이는 32 byte (256-bit) 이며, Base64 인코딩 형태로 환경변수에 보관한다.

```java
// AesEncryptor 사용 예
@Service
@RequiredArgsConstructor
public class OtpService {
    private final AesEncryptor aesEncryptor;

    public String storeSecret(String plainSecret) {
        return aesEncryptor.encrypt(plainSecret);   // IV prepend 포함
    }

    public String readSecret(String cipherSecret) {
        return aesEncryptor.decrypt(cipherSecret);
    }
}
```

### 3-3. JWT (must)

- JWT 서명 알고리즘은 HS256 또는 RS256 을 사용하며, 서명 비밀키는 `application.yml` 에서 외부화한다.
- JJWT 0.12.x 라이브러리를 사용하고, `Jwts.parser().verifyWith(key)` API 를 따른다.
- JWT secret 을 코드에 리터럴로 작성하거나 로그에 출력하지 않는다.

### 3-4. 복호화된 데이터 응답 (must)

- 복호화된 PII 를 응답에 포함할 때는 반드시 권한 검증 후에만 포함한다.
- 권한이 없는 사용자의 응답에는 마스킹된 값 또는 필드 제외로 처리한다.

### 3-5. 암호화 유틸 위치 (should)

- 신규 암호화 유틸은 `com.backend.global.util.` 패키지 아래에 추가한다.
- 도메인별 암호화 로직을 도메인 레이어에 직접 구현하지 않는다.

---

## 4. 예시

### 환경변수 설정 (application.yml)

```yaml
otp:
  encryption-key: ${OTP_ENCRYPTION_KEY}   # 32-byte, Base64
```

### SecurityConfig BCrypt 빈 등록

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## 5. 체크리스트

- [ ] 비밀번호는 BCrypt 해시 후 저장 — 평문 저장 없음
- [ ] `passwordEncoder.matches()` 로 비교 — 평문 직접 비교 없음
- [ ] `AesEncryptor` 사용 시 IV 가 `SecureRandom` 으로 생성됨
- [ ] 암호화 키가 `application.yml` 외부화 (`${...}`) 확인
- [ ] 코드 또는 git 에 평문 키 없음
- [ ] JWT secret 이 `application.yml` 외부화 확인
- [ ] 복호화 응답 전 권한 검증 로직 확인
- [ ] 신규 암호화 유틸이 `com.backend.global.util` 패키지에 위치
