# naming_rule.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 하 등급 작업자가 따라야 할 최소 명명 규칙을 정의한다.
상 등급의 Feature 표준 세트(Retrieve/Register/Verify…) 적용은 하 등급에서 강제하지 않는다.
PascalCase / camelCase / kebab-case 세 가지 표기 규칙만 강제한다.

## 변경/완화 사항

- 상 등급의 Feature 표준 세트(Retrieve/Register/Update/Delete…) 강제 → 하 등급은 미적용. RESTful 동사(get/create/update/delete) 허용
- 상 등급의 계층 전체 명칭 일관성 강제 → 하 등급은 각 계층 내 규칙만 준수
- 상 등급의 `com.poc.backend` 패키지 기준 → `com.backend` 사용
- 테이블명 변환 규칙 → 해당 없음 (JPA 사용)

## 이 등급에서 강제하는 것

**must**
- 클래스·인터페이스명: PascalCase (`UserController`, `PhoneVerifyRequest`)
- 메서드·변수명: camelCase (`getUserById`, `phoneNumber`)
- URI 경로 세그먼트: kebab-case 소문자 (`/api/v1/users`, `/me/phone/verify`)
- 상수: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`, `PHONE_REGEX`)

**should**
- 약어 사용을 피하고 의미 있는 이름을 사용한다.

## 예시 코드

```java
// PascalCase 클래스명
public class UserController { ... }
public record PhoneVerifyRequest(String phone, String code) {}

// camelCase 메서드·변수명
public UserResponse getUserById(String userId) { ... }
private final String accessToken;

// UPPER_SNAKE_CASE 상수
private static final int VERIFICATION_TTL_MINUTES = 5;
```

URI (domain/user/controller/UserController.java:28):

```java
@RequestMapping("/api/v1/users")  // kebab-case, /api/v1/{domain}
@GetMapping("/me")
@PostMapping("/me/phone/verify")  // kebab-case 세그먼트
```

## 체크리스트

- [ ] 클래스·인터페이스명이 PascalCase인가?
- [ ] 메서드·변수명이 camelCase인가?
- [ ] URI 경로 세그먼트가 kebab-case 소문자인가?
- [ ] 상수가 UPPER_SNAKE_CASE인가?
- [ ] 의미를 알 수 없는 단문자 변수명(`a`, `x`, `tmp`)을 사용하지 않는가?
