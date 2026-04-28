# naming_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 지켜야 할 핵심 명명 규칙을 정의한다.
케이스 규칙과 클래스 Suffix 규약 준수가 기본이다.

---

## 2. 기본 케이스 규칙 (must)

| 대상 | 케이스 | 예시 |
|------|--------|------|
| 클래스 · 인터페이스 · Enum · record | PascalCase | `UserService`, `UserController` |
| 메서드 · 변수 | camelCase | `getUser()`, `userId` |
| 상수 (`static final`) | UPPER_SNAKE_CASE | `PHONE_REGEX` |
| 패키지 | 소문자 | `com.backend.domain.user` |
| URL 경로 | kebab-case | `/api/v1/users` |

---

## 3. 클래스 Suffix 규약 (must)

| 역할 | Suffix | 예시 |
|------|--------|------|
| REST 컨트롤러 | `Controller` | `UserController` |
| 서비스 | `Service` | `UserService` |
| JPA Repository | `Repository` | `UserRepository` |
| 비즈니스 예외 | `Exception` | `UserNotFoundException` |
| JPA Entity | (단순 명사) | `User`, `Pet` |

---

## 4. URL 패턴

```
/api/v1/{domain}/{sub-resource}
```

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:28
@RequestMapping("/api/v1/users")
```

---

## 5. 체크리스트

- [ ] 클래스명이 PascalCase이고 적절한 Suffix(Controller/Service/Repository/Exception)를 갖는가
- [ ] 메서드·변수명이 camelCase인가
- [ ] URL 경로가 kebab-case이고 `/api/v1/{domain}` 구조인가
- [ ] JPA Entity 클래스명이 단순 명사 형태인가
