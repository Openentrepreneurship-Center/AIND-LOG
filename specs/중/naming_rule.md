# naming_rule.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 클래스, 메서드, 변수, URI의 명명 규칙을 정의한다.
상 등급의 Feature 표준 세트(Retrieve/Register/Update/Delete…)는 이 등급에서 선택적으로 적용한다.
RESTful 동사(create/update/delete/find/get)도 허용하며 Feature 세트와 혼용하지 않는 선에서 사용 가능하다.
패키지 루트는 `com.backend`이며 도메인 엔티티는 단순 명사(User, Pet)를 사용한다.

## 변경/완화 사항

- 상 등급의 Feature 표준 세트(Retrieve/Register/Verify…) 강제 → 중 등급은 권장. RESTful 동사(get/create/update/delete/find/send/verify) 허용
- 상 등급의 `com.poc.backend` 패키지 기준 → 본 프로젝트는 `com.backend` 사용
- 상 등급의 MyBatis XML Mapper 명명(AutnMapper.xml 등) → 해당 없음. Entity는 단순 PascalCase 명사 사용
- 테이블명 접두·접미 제거 규칙(`TB_`, `_M`, `_AC` 등) → JPA 사용이므로 테이블명 변환 규칙 미적용

## 이 등급에서 강제하는 것

**must**
- 클래스·인터페이스 이름은 PascalCase 명사형으로 작성한다. (`UserController`, `PhoneVerifyRequest`)
- 메서드·변수 이름은 camelCase 동사형으로 작성한다. (`getUser`, `verifyPhoneChange`, `userId`)
- URI는 `/api/v1/{domain}` 기본 prefix를 사용하며, 경로 세그먼트는 kebab-case 소문자로 작성한다.
  - 예: `@RequestMapping("/api/v1/users")`, `@GetMapping("/me/phone/verify")`
- 도메인 엔티티 클래스는 단순 명사를 사용한다. (`User`, `Pet`, `Verification` — `UserEntity`처럼 접미사 붙이지 않음)
- DTO 클래스명은 `{명사구}{Request|Response}` 형식으로 작성한다. (`PhoneVerifyRequest`, `UserResponse`)
- 상수는 UPPER_SNAKE_CASE로 선언한다. (`VERIFICATION_TTL_MINUTES`, `PHONE_REGEX`)
- Controller 클래스명은 `{Domain}Controller`, Service는 `{Domain}Service` 형식을 유지한다.

**should**
- Feature 표준 세트(Retrieve/Register/Verify 등)를 사용하면 계층(Controller, Service, DTO) 전체에서 동일 명칭을 유지한다.
- 약어 사용을 자제하고 의미 있는 단어를 사용한다. (`userId` not `uid`, `accessToken` not `at`)

## 예시 코드

Controller URI + 메서드명 패턴 (domain/user/controller/UserController.java:27-90):

```java
// com.backend.domain.user.controller.UserController
@RestController
@RequestMapping("/api/v1/users")   // /api/v1/{domain}
@RequiredArgsConstructor
public class UserController implements UserApi {

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getMe(@AuthenticationPrincipal String userId) { ... }

    @PostMapping("/me/phone/sms/send")
    public ResponseEntity<SuccessResponse> sendPhoneChangeSms(...) { ... }

    @PostMapping("/me/phone/verify")
    public ResponseEntity<SuccessResponse> verifyPhoneChange(...) { ... }
}
```

DTO record 명명:

```java
// com.backend.domain.user.dto.request.PhoneVerifyRequest  ← {명사구}{Request}
public record PhoneVerifyRequest(String phone, String code) {}

// com.backend.domain.user.dto.response.UserResponse       ← {명사구}{Response}
public record UserResponse(String id, String email, String phone) {}
```

## 체크리스트

- [ ] 클래스명이 PascalCase 명사형인가? (ex. `UserController`, `PhoneVerifyRequest`)
- [ ] 메서드·변수명이 camelCase인가?
- [ ] URI가 `/api/v1/{domain}` prefix를 사용하고 kebab-case인가?
- [ ] 도메인 엔티티에 `Entity` 접미사를 붙이지 않았는가? (`User` not `UserEntity`)
- [ ] DTO 클래스명이 `{명사구}{Request|Response}` 형식인가?
- [ ] 상수가 UPPER_SNAKE_CASE로 선언되었는가?
- [ ] 동일 기능의 Controller 메서드명, Service 메서드명, DTO명이 일관된 명사구를 사용하는가?
