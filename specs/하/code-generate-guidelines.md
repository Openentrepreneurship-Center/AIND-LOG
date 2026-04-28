# code-generate-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 코드 생성 도구가 `com.backend` 프로젝트에서 코드를 생성·수정할 때
반드시 지켜야 하는 최소 규칙을 정의한다.

---

## 2. 핵심 원칙

### 2.1 기존 파일 재생성 금지 (must)
- 기존 파일을 삭제·이동·리네임하지 않는다.
- 기존 파일을 통째로 재생성하여 덮어쓰지 않는다.
- 새 기능은 기존 파일에 메서드·라인을 추가하거나 신규 파일을 생성하는 방식으로만 구현한다.

### 2.2 public 메서드 시그니처 보존 (must)
- 기존 `public` 메서드의 이름, 파라미터, 반환 타입을 변경하지 않는다.
- 기존 `public` 메서드를 삭제하지 않는다.

```java
// 예시: 기존 메서드 유지하며 신규 메서드 추가
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:51-54
@Transactional(readOnly = true)
public UserResponse getUser(String userId) {
    User user = userRepository.getByIdAndDeletedAtIsNull(userId);
    return userResponseMapper.toResponse(user);
}
// ↑ 이 메서드 시그니처는 변경하지 않는다.
```

---

## 3. 체크리스트

- [ ] 기존 파일을 삭제·이동·리네임하지 않았는가
- [ ] 기존 파일을 통째로 재생성하지 않았는가
- [ ] 기존 `public` 메서드 시그니처가 유지되는가
- [ ] 기존 `@Transactional` 경계가 변경되지 않았는가
