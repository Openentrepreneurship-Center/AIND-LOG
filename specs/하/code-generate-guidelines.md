# code-generate-guidelines.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 코드를 추가할 때 하 등급 작업자가 따라야 할 최소 규칙을 정의한다.
"단순 추가만 허용"이 핵심이다. 기존 시그니처와 파일 구조를 건드리지 않는다.

## 변경/완화 사항

- 상 등급의 Unified Diff 출력 원칙 → 하 등급은 적용하지 않음
- 상 등급의 `[EXISTING]` / `[TO_BE_CREATED]` / `[INTERFACE]` 의존성 분류 → 하 등급은 미적용
- MyBatis XML Mapper 규칙 → 해당 없음

## 이 등급에서 강제하는 것

**must**
- 기존 파일을 삭제·리네임·이동하지 않는다.
- 기존 public 메서드의 이름, 파라미터 타입·순서, 반환 타입을 변경하지 않는다.
- 신규 기능은 신규 파일 또는 기존 파일 끝에 신규 메서드 추가로만 구현한다.

**should**
- JPA Entity에 필드를 추가할 때 Flyway 마이그레이션 스크립트를 함께 작성한다.
- 기존 테스트가 깨지지 않도록 한다.

## 예시 코드

신규 메서드 추가 (기존 메서드 변경 금지):

```java
// 기존 메서드 — 변경 금지
public UserResponse getUser(String userId) { ... }

// 신규 메서드 — 기존 파일 끝에 추가
public List<UserResponse> getUsersByStatus(String status) {
    // 구현
}
```

## 체크리스트

- [ ] 기존 파일을 삭제·리네임·이동하지 않았는가?
- [ ] 기존 public 메서드 시그니처를 변경하지 않았는가?
- [ ] 신규 기능이 신규 파일 또는 신규 메서드 추가로만 구현되었는가?
- [ ] 기존 테스트가 여전히 통과하는가?
