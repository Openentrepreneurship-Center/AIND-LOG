# spec-driven-buisness-rule-guidlines.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 decapet-official/backend 에서 비즈니스 규칙을 코드에 명시하는 기준을 정의한다.
상 등급의 PGM-ID 는 외부 시스템 연계 의존이 없는 이 프로젝트에서 강제하지 않는다.
대신 도메인 핵심 메서드(인증 · 결제 · 권한 변경)에 한해 RULE-ID 주석을 권장하며,
PR 설명에 영향 받는 RULE-ID 를 명시하여 추적성을 확보한다.

---

## 2. 변경/완화 사항

| 상 등급 항목 | 중 등급 변경 내용 |
|---|---|
| PGM-ID 주석 (PGM-XXX) 강제 | 면제 — 외부 시스템 연계가 없으므로 불필요 |
| RULE-ID 모든 메서드 적용 | 도메인 핵심 메서드(인증/결제/정산/권한)에 한해 권장(should) |
| RULE-ID 주석 삭제 금지 강제 | 동일하게 유지 — 한번 선언된 RULE-ID 주석은 삭제하지 않는다 |
| 구현 완료 여부 전수 검토 | 핵심 도메인에 한해 PR 리뷰 시 검토 |

---

## 3. 강제 사항

### 3-1. RULE-ID 주석 형식 (should)

- 인증 · 결제 · 정산 · 권한 변경 등 핵심 비즈니스 메서드에 RULE-ID 주석을 작성한다.
- 형식: `// RULE: {도메인}-{기능}-{순번}` (예: `// RULE: USER-LOGIN-001`)
- 메서드 시작부 javadoc 또는 인라인 주석 두 곳에 모두 작성하면 가장 좋다.

```java
/**
 * SMS OTP 인증을 검증하고 전화번호를 변경한다.
 * RULE: USER-PHONE-001 - 인증번호는 5분 이내에만 유효하다.
 * RULE: USER-PHONE-002 - 인증 성공 후 기존 OTP 는 즉시 무효화한다.
 */
@Transactional
public void verifyPhone(String userId, PhoneVerifyRequest request) {
    // RULE: USER-PHONE-001
    otpService.validate(request.phone(), request.code());
    // RULE: USER-PHONE-002
    otpService.invalidate(request.phone());
    userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
        .updatePhone(request.phone());
}
```

### 3-2. RULE-ID 유지 (must)

- 한번 선언된 RULE-ID 주석은 비즈니스 규칙이 변경되더라도 삭제하지 않는다.
- 규칙 변경 시 기존 RULE-ID 에 변경 사유 주석을 추가하거나, 신규 RULE-ID 를 병기한다.

### 3-3. PR 설명 (must)

- 핵심 비즈니스 규칙에 영향을 주는 PR 은 본문에 관련 RULE-ID 를 명시한다.
- 형식 예시: `영향 규칙: USER-PHONE-001, USER-PHONE-002`

### 3-4. 코드 품질 (must)

- 병합 전 lint 오류 · 컴파일 오류 없음을 확인한다.
- RULE-ID 가 선언된 메서드는 단위 테스트를 1개 이상 작성한다.

---

## 4. 예시

### RULE-ID 선언 패턴

```java
// com.backend.domain.user.service.UserService

/**
 * RULE: USER-LOGIN-001 - 탈퇴 또는 정지 계정은 로그인을 허용하지 않는다.
 * RULE: USER-LOGIN-002 - 로그인 성공 시 마지막 로그인 시각을 갱신한다.
 */
@Transactional
public TokenResponse login(LoginRequest request) {
    // RULE: USER-LOGIN-001
    User user = userRepository.findByEmail(request.email())
        .filter(u -> !u.isDeleted() && u.isActive())
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    // RULE: USER-LOGIN-002
    user.updateLastLoginAt(LocalDateTime.now());
    return tokenProvider.generate(user.getId());
}
```

### PR 본문 예시

```
## 변경 사항
- OTP 인증 만료 시간을 5분에서 3분으로 단축

## 영향 규칙
- RULE: USER-PHONE-001 (만료 시간 조건 변경)
```

---

## 5. 체크리스트

- [ ] 인증 · 결제 · 권한 변경 메서드에 RULE-ID 주석 작성
- [ ] RULE-ID 형식이 `RULE: {도메인}-{기능}-{순번}` 를 따름
- [ ] 기존 RULE-ID 주석이 삭제되지 않음
- [ ] 핵심 비즈니스 규칙 변경 PR 본문에 RULE-ID 명시
- [ ] RULE-ID 선언 메서드에 단위 테스트 1개 이상 존재
- [ ] 병합 전 컴파일 오류 · lint 오류 없음 확인
