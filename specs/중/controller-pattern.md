# controller-pattern.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

컨트롤러는 HTTP 요청 수신, 입력 검증, 서비스 위임, 응답 직렬화만 담당한다.
비즈니스 로직은 서비스 계층에 위임하고, 컨트롤러 내부에서 연산·판단을 직접 구현하지 않는다.
URL은 `/api/v1/{domain}` 패턴을 따르며, 인증 사용자 식별은 `@AuthenticationPrincipal String userId`로 통일한다.
공통 응답 타입은 `ResponseEntity<SuccessResponse>`이며, `SuccessResponse.of(SuccessCode, data)` 팩토리를 사용한다.
Swagger 문서화(`@Operation`)는 별도 `*Api` 인터페이스에 선언하고, 구현체는 `implements *Api`로 분리한다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상 등급 | 중 등급 |
|------|---------|---------|
| `@Validated` 클래스 어노테이션 | 필수 | 권장 (메서드 파라미터 검증이 `@Valid`로 충분하면 생략 가능) |
| `@Slf4j` + 개별 로그 | 필수 | 권장 (간단한 CRUD는 생략 허용) |
| Swagger `@Operation` | 컨트롤러 직접 선언 | `*Api` 인터페이스 분리 선언 강제 |
| 응답 타입 | `ApiResponse<T>` (상 등급 프로젝트 공통) | `SuccessResponse` (decapet 공통 타입) 필수 |

---

## 3. 강제 사항

### must (위반 시 PR 반려)
- `@RestController` + `@RequestMapping("/api/v1/{domain}")` 적용
- 모든 요청 DTO 파라미터에 `@Valid` 적용
- 응답은 `ResponseEntity<SuccessResponse>` 반환
- 인증 사용자 식별은 `@AuthenticationPrincipal String userId` 사용 (세션/헤더 직접 파싱 금지)
- 컨트롤러 내 비즈니스 로직(조건 분기, DB 조회 등) 구현 금지
- 도메인별 컨트롤러 1개 원칙 (관리자/일반 분리는 허용: `UserController` + `AdminUserController`)

### should (강력 권장)
- `@RequiredArgsConstructor` 생성자 주입
- Swagger 문서는 `*Api` 인터페이스로 분리 선언
- 성공 코드는 `SuccessCode` enum 상수 사용 (`SuccessCode.USER_GET_SUCCESS` 등)
- 복합 헤더 응답 시 `ResponseEntity.ok().headers(...).body(...)` 체인 사용

---

## 4. 예시 코드

실제 `com.backend.domain.user.controller.UserController` 패턴 인용:

```java
// decapet-official/backend/.../user/controller/UserController.java:27-89
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final CookieUtil cookieUtil;

    @Override
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getMe(@AuthenticationPrincipal String userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
    }

    @Override
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse> deleteAccount(@AuthenticationPrincipal String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.USER_DELETE_SUCCESS));
    }
}
```

DTO 검증 패턴 (`PhoneVerifyRequest.java:9-20`):

```java
// decapet-official/backend/.../user/dto/request/PhoneVerifyRequest.java:9-20
public record PhoneVerifyRequest(
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        String phone,

        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {}
```

`SuccessResponse` 팩토리 패턴:

```java
// decapet-official/backend/.../global/common/SuccessResponse.java:14-18
public static SuccessResponse of(SuccessCode successCode) {
    return new SuccessResponse(successCode, null);
}
public static SuccessResponse of(SuccessCode successCode, Object data) {
    return new SuccessResponse(successCode, data);
}
```

---

## 5. 체크리스트

- [ ] `@RestController` + `@RequestMapping("/api/v1/{domain}")` 선언되었는가
- [ ] `@RequiredArgsConstructor` 생성자 주입을 사용하는가
- [ ] 모든 요청 DTO에 `@Valid` 어노테이션이 적용되었는가
- [ ] 인증 사용자 식별에 `@AuthenticationPrincipal String userId`를 사용하는가
- [ ] 응답 타입이 `ResponseEntity<SuccessResponse>`인가
- [ ] `SuccessResponse.of(SuccessCode.XXX, data)` 팩토리를 사용하는가
- [ ] 컨트롤러 내에 비즈니스 로직(DB 조회, 조건 분기)이 없는가
- [ ] Swagger 문서(`@Operation`)가 `*Api` 인터페이스에 선언되었는가
- [ ] 도메인당 컨트롤러가 2개 이하(일반 + 관리자)인가
- [ ] `SuccessCode` enum 상수를 하드코딩 문자열 없이 사용하는가
