# spec-driven-buisness-rule-guidlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 PR을 제출할 때 도메인명을 PR 제목에 포함하여 변경 범위를 명확히 하는 최소 규칙을 정의한다.

---

## 2. 원칙

- PR 제목만 보고 어느 도메인에 영향을 주는 변경인지 파악할 수 있어야 한다.
- RULE-ID, PGM-ID 주석은 이 문서의 의무 적용 범위에서 제외한다.

---

## 3. 강제 사항

**must**

- PR 제목에 변경이 발생한 도메인명을 반드시 포함한다.

  ```
  # 올바른 PR 제목 예시
  feat(payment): 결제 승인 금액 검증 로직 추가
  fix(appointment): 중복 예약 방지 조건 수정
  refactor(user): 사용자 상태 전이 메서드 분리
  chore(auth): JWT 만료 시간 설정 외부화

  # 금지 — 도메인명 없는 제목
  feat: 버그 수정
  fix: 로직 변경
  ```

- 도메인명은 `com.backend.domain.{도메인}` 패키지명을 기준으로 한다.

  | 패키지 | PR 제목 도메인명 |
  |--------|----------------|
  | `com.backend.domain.user` | `user` |
  | `com.backend.domain.payment` | `payment` |
  | `com.backend.domain.appointment` | `appointment` |
  | `com.backend.domain.order` | `order` |
  | `com.backend.domain.prescription` | `prescription` |
  | `com.backend.global` | `global` |

- 여러 도메인에 걸친 변경은 주 영향 도메인을 기준으로 제목을 작성하고, 본문에 영향 범위를 기술한다.

---

## 4. 코드 예시 (decapet 인용)

```java
// com.backend.global.error.ErrorCode — 도메인별 에러 코드 접두사가 도메인명 기준
// User: U001, Payment: PM001, Appointment: AP001, Order: O001
// PR 제목의 도메인명과 ErrorCode 접두사를 일치시켜 추적성을 높인다

PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PM007", "결제 금액이 일치하지 않습니다."),
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
```

---

## 5. 체크리스트

- [ ] PR 제목에 도메인명 포함 (`feat(payment):`, `fix(user):` 등)
- [ ] 도메인명이 `com.backend.domain.{도메인}` 패키지명과 일치
- [ ] 여러 도메인 변경 시 PR 본문에 영향 범위 기술
