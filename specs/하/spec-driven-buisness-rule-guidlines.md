# spec-driven-buisness-rule-guidlines.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 RULE-ID 주석과 PGM-ID 를 모두 면제한다.
단, PR 제목에 변경 대상 도메인명을 포함하는 것만 강제하여 최소한의 추적성을 유지한다.

---

## 2. 변경/완화 사항

| 상/중 등급 항목 | 하 등급 변경 내용 |
|---|---|
| PGM-ID 주석 | 면제 |
| RULE-ID 메서드 주석 | 면제 |
| RULE-ID 인라인 주석 | 면제 |
| RULE-ID 삭제 금지 | 면제 (RULE-ID 를 작성하지 않으므로) |
| PR 본문 RULE-ID 명시 | 면제 |
| PR 제목 도메인명 포함 | 강제 |

---

## 3. 강제 사항 (1개)

### 규칙 1. PR 제목 도메인명 포함 (must)

PR 제목에 변경한 도메인 이름을 반드시 포함한다.
형식은 자유이나, 도메인을 특정할 수 있어야 한다.

```
// 허용 예
feat(user): 전화번호 인증 로직 추가
fix(payment): 결제 금액 계산 오류 수정
refactor(pet): 동물등록번호 검증 로직 분리

// 금지 예
fix: 버그 수정
update: 코드 개선
```

---

## 4. 예시

도메인명 후보: `user`, `pet`, `payment`, `auth`, `notification`, `address`

---

## 5. 체크리스트

- [ ] PR 제목에 도메인명 포함 (`user`, `payment`, `auth` 등)
- [ ] 컴파일 오류 없음 확인 후 PR 생성
