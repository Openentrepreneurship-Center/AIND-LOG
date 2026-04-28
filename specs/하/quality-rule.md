# quality-rule.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 최소한의 보안 사고를 방지하기 위한 4가지 필수 규칙만 강제한다.
도메인 검증, 트랜잭션 세분화, Soft Delete 정책 등 중/상 등급 권고는 선택 적용이며,
아래 4개 항목만큼은 모든 PR 에서 반드시 준수해야 한다.

---

## 2. 변경/완화 사항

| 상/중 등급 항목 | 하 등급 변경 내용 |
|---|---|
| Controller 전체 보안 규칙 | `@Valid` 적용 여부만 강제 |
| JPA 파라미터 바인딩 전 항목 | 문자열 concat JPQL 금지만 강제 |
| 로깅 전체 마스킹 | 비밀번호 · 토큰 미로깅만 강제 |
| `Pageable` + `Page<T>` 필수 | 목록 조회에 `Pageable` 사용만 강제 |
| Soft Delete, 감사 트랜잭션, DTO 변환 | 권고 (강제 아님) |

---

## 3. 강제 사항 (4개)

### 규칙 1. @Valid 적용 (must)

`@RequestBody` 와 `@ModelAttribute` 에는 반드시 `@Valid` 를 붙인다.
`com.backend.global.common.constants.ValidationConstants` 의 정규식 상수를 `@Pattern` 에 참조한다.

```java
@PostMapping("/phone/verify")
public ResponseEntity<?> verify(@Valid @RequestBody PhoneVerifyRequest request) { ... }
```

### 규칙 2. JPA 파라미터 바인딩 (must)

JPQL 을 문자열 concat 으로 조립하는 행위를 금지한다.
모든 값은 `@Param` / `:param` / `?1` 바인딩을 사용한다.

```java
// 금지
String q = "SELECT u FROM User u WHERE u.email = '" + email + "'";

// 허용
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);
```

### 규칙 3. 비밀번호 · 토큰 미로깅 (must)

비밀번호 · JWT 토큰 · OTP 코드를 로그에 출력하지 않는다.
객체 전체 `toString()` 으로 로그를 남길 때 이 필드가 포함되지 않도록 한다.

```java
// 금지
log.debug("로그인 요청: {}", loginRequest);   // password 포함 가능

// 허용
log.debug("로그인 시도: email={}", loginRequest.email());
```

### 규칙 4. Pageable 페이징 (must)

목록 조회 Repository 메서드는 `Pageable` 을 파라미터로 받고 `Page<T>` 를 반환한다.
무제한 `findAll()` 을 운영 환경 API 에 직접 노출하지 않는다.

```java
// 허용
Page<User> findAll(Pageable pageable);
```

---

## 4. 예시

```java
// com.backend.domain.user.dto.request.PhoneVerifyRequest 패턴 참조
public record SearchRequest(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
```

---

## 5. 체크리스트

- [ ] `@RequestBody` / `@ModelAttribute` 에 `@Valid` 적용
- [ ] 문자열 concat JPQL 없음
- [ ] 비밀번호 · 토큰 로그 출력 없음
- [ ] 목록 조회에 `Pageable` 파라미터 사용
