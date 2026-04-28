# controller-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

컨트롤러는 HTTP 요청 수신, 입력 검증, 서비스 위임, 공통 응답 반환만 담당한다. 비즈니스 로직은 서비스 계층에 위임한다. 공통 응답 형식은 `com.backend.global.common.SuccessResponse`를 사용하며, 인증 사용자는 `@AuthenticationPrincipal String userId`로 수신한다.

---

## 2. 원칙 / 패턴 설명

### 2.1 클래스 구조

```
com.backend.domain.{x}.controller
├── {Domain}Api.java         // Swagger 명세 인터페이스 (권장)
└── {Domain}Controller.java  // 실제 구현체
```

### 2.2 URL 규칙

- 클래스 레벨: `@RequestMapping("/api/v1/{domain}s")`
- 메서드 레벨: HTTP 메서드 어노테이션 + 경로
- 현재 사용자 자원: `/me`, `/me/phone/verify`

### 2.3 응답 형식

```java
// 데이터 있음
ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));

// 데이터 없음
ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_DELETE_SUCCESS));
```

---

## 3. 강제 사항

### must

- `@RestController` 선언
- 클래스 레벨 `@RequestMapping("/api/v1/{domain}")` 선언
- `@RequiredArgsConstructor` (생성자 주입)
- `@RequestBody` 파라미터에 `@Valid` 부착
- `@AuthenticationPrincipal String userId` 사용
- 반환 타입 `ResponseEntity<SuccessResponse>`

### should

- Swagger `@Operation` + `@ApiResponses` 를 `*Api` 인터페이스에 작성
- `@SecurityRequirement(name = "cookieAuth")` 인증 엔드포인트에 부착

---

## 4. 예시 코드

### 4.1 컨트롤러 구현

`decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:27-76`

```java
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
    @PostMapping("/me/phone/verify")
    public ResponseEntity<SuccessResponse> verifyPhoneChange(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid PhoneVerifyRequest request) {
        userService.verifyPhoneChange(userId, request.phone(), request.code());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_PHONE_CHANGE_SUCCESS));
    }
}
```

### 4.2 Request DTO

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:9-19`

```java
public record PhoneVerifyRequest(
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
    String phone,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
    String code
) {}
```

---

## 5. 체크리스트

- [ ] `@RestController` 선언
- [ ] 클래스 레벨 `@RequestMapping("/api/v1/{domain}")` 설정
- [ ] `@RequiredArgsConstructor` 선언
- [ ] `@RequestBody` 파라미터에 `@Valid` 부착
- [ ] 인증 필요 메서드에 `@AuthenticationPrincipal String userId` 선언
- [ ] 반환 타입 `ResponseEntity<SuccessResponse>`
- [ ] 서비스 호출 + 응답 반환만 포함 (비즈니스 로직 없음)
- [ ] Request DTO는 Java `record` + 검증 어노테이션 사용
- [ ] Swagger `@Operation` + `@ApiResponses` 작성 (권장)
- [ ] `@SecurityRequirement(name = "cookieAuth")` 인증 엔드포인트에 부착 (권장)
