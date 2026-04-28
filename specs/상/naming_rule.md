# naming_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전체에 걸쳐 적용되는 이름 부여 규칙을 정의한다.
클래스, 메서드, 변수, 패키지, URL 엔드포인트 모든 계층에서 일관된 명명 규칙을 따름으로써
코드 탐색 비용을 낮추고 자동화 도구(코드 생성, 검색)의 정확도를 높인다.
아래 규칙은 신규 코드 작성 및 기존 코드 수정 모두에 적용된다.

---

## 2. 기본 케이스 규칙 (must)

| 대상 | 케이스 | 예시 |
|------|--------|------|
| 클래스 · 인터페이스 · Enum · record | PascalCase | `UserService`, `PhoneVerifyRequest`, `ErrorCode` |
| 메서드 · 변수 · 파라미터 | camelCase | `getUser()`, `userId`, `verificationCode` |
| 상수 (`static final`) | UPPER_SNAKE_CASE | `VERIFICATION_TTL_MINUTES`, `PHONE_REGEX` |
| 패키지 | 소문자 단어 연속, 언더스코어 금지 | `com.backend.domain.user.controller` |
| URL 경로 세그먼트 | kebab-case (소문자 + 하이픈) | `/api/v1/users`, `/me/phone/sms/send` |

---

## 3. 클래스 Suffix 규약 (must)

| 역할 | Suffix | 예시 |
|------|--------|------|
| REST 컨트롤러 | `Controller` | `UserController`, `AdminUserController` |
| Swagger 명세 인터페이스 | `Api` | `UserApi`, `AdminUserApi` |
| 서비스 | `Service` | `UserService`, `SmsService` |
| JPA Repository | `Repository` | `UserRepository`, `VerificationRepository` |
| Entity ↔ DTO 변환기 | `Mapper` | `UserMapper`, `UserResponseMapper` |
| 비즈니스 예외 | `Exception` | `UserNotFoundException`, `DuplicateEmailException` |
| JPA Entity | (단순 명사, suffix 없음) | `User`, `Pet`, `Verification` |
| 설정 클래스 | `Config` | `SecurityConfig`, `JpaConfig`, `RateLimitConfig` |
| 필터 | `Filter` | `JwtFilter`, `RateLimitFilter`, `AccountValidationFilter` |
| 이벤트 | `Event` | `UserDeletedEvent` |
| 이벤트 리스너 | `EventListener` | `UserDeletionEventListener` |

---

## 4. URL 패턴 (must)

### 4.1 기본 구조
```
/api/v1/{domain}/{sub-resource}
```

- `{domain}`: 도메인 복수 명사, kebab-case (`users`, `medicine-carts`, `custom-products`)
- `{sub-resource}`: 추가 자원 경로, kebab-case

### 4.2 RESTful 동사 매트릭스

| 동작 | HTTP 메서드 | URL 예시 |
|------|-------------|----------|
| 단건 조회 | GET | `/api/v1/users/me` |
| 목록 조회 | GET | `/api/v1/users` |
| 생성 | POST | `/api/v1/pets` |
| 수정 (부분) | PATCH | `/api/v1/users/me` |
| 수정 (전체) | PUT | `/api/v1/users/me` |
| 삭제 | DELETE | `/api/v1/users/me` |
| 액션 (동사) | POST | `/api/v1/users/me/phone/sms/send`, `/api/v1/users/me/phone/verify` |

- URL에 동사를 사용하는 경우는 상태 변환·액션에만 한정하며, 계층 구조로 표현 불가능한 경우에만 허용한다.
- 실제 코드 예:

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:28,61,70
@RequestMapping("/api/v1/users")
// ...
@PostMapping("/me/phone/sms/send")
// ...
@PostMapping("/me/phone/verify")
```

---

## 5. DTO 이름 규칙 (must)

### 5.1 형식
```
{Action}{Entity}{Request|Response}
```

| 요소 | 설명 | 예시 |
|------|------|------|
| `{Action}` | 동작 의미 동사 (PascalCase) | `Update`, `Change`, `Phone`, `BulkUpdate` |
| `{Entity}` | 대상 도메인 엔티티 | `Profile`, `Password`, `Phone`, `User` |
| `{Request}` | 입력 DTO | `UpdateProfileRequest`, `ChangePasswordRequest` |
| `{Response}` | 출력 DTO | `UserResponse`, `AdminUserListResponse` |

### 5.2 내부 변환용 DTO
- 계층 간 내부 전달 객체는 `{명사}Info` 형식을 사용한다.
- 패키지: `com.backend.domain.{x}.dto.internal`
- 예: `ProfileUpdateInfo`, `VerificationCreateInfo`

### 5.3 DTO는 Java record (must)
- 모든 요청·응답 DTO는 `record`로 선언한다.
- 내부 변환용 DTO(`*Info`)도 `record`를 원칙으로 한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:9
public record PhoneVerifyRequest(
        String phone,
        String code
) {}
```

---

## 6. Entity 이름 규칙 (must)

- Entity 클래스명은 단순 명사 형태로, suffix를 붙이지 않는다.
- 테이블명은 스네이크케이스 복수 명사로 JPA `@Table`에서 지정한다.
- Enum 타입은 `{명사}Type` 또는 의미를 나타내는 단순 명사 형태를 사용한다.

| 클래스 | 설명 |
|--------|------|
| `User` | 사용자 엔티티 |
| `Pet` | 반려동물 엔티티 |
| `PermissionType` | 권한 유형 Enum |

---

## 7. 서비스 메서드 이름 규칙 (should)

| 패턴 | 의미 | 예시 |
|------|------|------|
| `get{Entity}` | 단건 조회 (없으면 예외) | `getUser()` |
| `list{Entity}` | 목록 조회 | `listUsers()` |
| `create{Entity}` | 생성 | `createPet()` |
| `update{Entity}` | 수정 | `updateProfile()` |
| `delete{Entity}` | 삭제 | `deleteUser()` |
| `send{명사}` | 발송 | `sendPhoneChangeSms()` |
| `verify{명사}` | 검증 | `verifyPhoneChange()` |
| `change{명사}` | 변경 | `changePassword()` |
| `validate{명사}` | 유효성 확인 (예외 발생형) | `validateEmailNotDuplicate()` |

---

## 8. Repository 메서드 이름 규칙 (must)

- Spring Data JPA 파생 쿼리는 `findBy{조건}` 패턴을 따른다.
- 조회 결과가 없으면 예외를 던지는 래퍼 메서드는 `getBy{조건}` 접두사를 사용한다.
- soft-delete 대상은 조건에 `AndDeletedAtIsNull`을 반드시 포함한다.
- 유효성 검증 메서드는 `validate{명사}` 접두사를 사용한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:39-42
default User getByIdAndDeletedAtIsNull(String id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(UserNotFoundException::new);
}
```

---

## 9. 상수 클래스 이름 규칙 (must)

- 공용 검증 정규식·메시지는 `ValidationConstants`에 모아둔다.
- 클래스는 `private` 생성자를 두어 인스턴스화를 방지한다.
- 상수명은 `{대상}_{구분자}` 형태: `PHONE_REGEX`, `PHONE_MESSAGE`.

```java
// decapet-official/backend/src/main/java/com/backend/global/common/constants/ValidationConstants.java:3-6
public final class ValidationConstants {
    private ValidationConstants() {}
    public static final String PHONE_REGEX = "^010\\d{8}$";
}
```

---

## 10. 체크리스트

- [ ] 클래스명이 PascalCase이고 적절한 Suffix(`Controller`/`Service`/`Repository`/`Mapper`/`Exception`)를 갖는가
- [ ] 메서드·변수명이 camelCase인가
- [ ] 상수가 UPPER_SNAKE_CASE인가
- [ ] URL 경로 세그먼트가 kebab-case이고 `/api/v1/{domain}` 구조를 따르는가
- [ ] 요청 DTO가 `{Action}{Entity}Request` 형식의 `record`인가
- [ ] 응답 DTO가 `{Action}{Entity}Response` 또는 `{Entity}Response` 형식의 `record`인가
- [ ] Entity 클래스가 단순 명사 형태(suffix 없음)인가
- [ ] Repository `default` 래퍼 메서드가 `getBy` 접두사를 사용하는가
- [ ] soft-delete 조건에 `AndDeletedAtIsNull`이 포함되어 있는가
- [ ] 검증 정규식이 `ValidationConstants`에 상수로 선언되어 있는가
