# naming_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전체에 적용되는 명명 규칙을 정의한다.
클래스·메서드·URL·DTO 이름의 일관성을 유지해 코드 탐색 비용을 낮추고
자동화 도구가 예측 가능한 구조로 코드를 생성할 수 있도록 한다.

---

## 2. 기본 케이스 규칙 (must)

| 대상 | 케이스 | 예시 |
|------|--------|------|
| 클래스 · 인터페이스 · Enum · record | PascalCase | `UserService`, `PhoneVerifyRequest` |
| 메서드 · 변수 · 파라미터 | camelCase | `getUser()`, `userId` |
| 상수 (`static final`) | UPPER_SNAKE_CASE | `VERIFICATION_TTL_MINUTES` |
| 패키지 | 소문자, 언더스코어 금지 | `com.backend.domain.user.controller` |
| URL 경로 세그먼트 | kebab-case | `/api/v1/users`, `/me/phone/sms/send` |

---

## 3. 클래스 Suffix 규약 (must)

| 역할 | Suffix | 예시 |
|------|--------|------|
| REST 컨트롤러 | `Controller` | `UserController` |
| Swagger 명세 인터페이스 | `Api` | `UserApi` |
| 서비스 | `Service` | `UserService` |
| JPA Repository | `Repository` | `UserRepository` |
| Entity ↔ DTO 변환기 | `Mapper` | `UserMapper`, `UserResponseMapper` |
| 비즈니스 예외 | `Exception` | `UserNotFoundException` |
| JPA Entity | (단순 명사) | `User`, `Pet` |
| 설정 클래스 | `Config` | `SecurityConfig` |
| 필터 | `Filter` | `JwtFilter` |

---

## 4. URL 패턴 (must)

```
/api/v1/{domain}/{sub-resource}
```

- `{domain}`: 복수 명사, kebab-case (`users`, `medicine-carts`)
- 액션 엔드포인트는 POST + 경로 동사 허용 (`/me/phone/sms/send`)

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:28
@RequestMapping("/api/v1/users")
```

---

## 5. DTO 이름 규칙 (should)

- 요청 DTO: `{Action}{Entity}Request` 형식 권장
- 응답 DTO: `{Entity}Response` 또는 `{Action}{Entity}Response` 형식 권장
- 내부 전달 DTO: `{명사}Info` 형식 (`ProfileUpdateInfo`)

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:9
public record PhoneVerifyRequest(String phone, String code) {}
```

---

## 6. Repository 메서드 규칙 (must)

- `findBy{조건}`: Spring Data JPA 파생 쿼리
- `getBy{조건}`: 없으면 예외를 던지는 default 래퍼
- soft-delete 조건에 `AndDeletedAtIsNull` 포함

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:39-42
default User getByIdAndDeletedAtIsNull(String id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(UserNotFoundException::new);
}
```

---

## 7. 체크리스트

- [ ] 클래스명이 PascalCase이고 적절한 Suffix를 갖는가
- [ ] 메서드·변수명이 camelCase인가
- [ ] URL 경로가 kebab-case이고 `/api/v1/{domain}` 구조인가
- [ ] Repository `default` 래퍼 메서드가 `getBy` 접두사를 사용하는가
- [ ] soft-delete 조건에 `AndDeletedAtIsNull`이 포함되어 있는가
- [ ] 정규식 상수가 `ValidationConstants`에 위치하는가
