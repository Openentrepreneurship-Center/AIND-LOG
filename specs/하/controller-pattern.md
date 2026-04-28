# controller-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

컨트롤러는 HTTP 요청을 받아 서비스에 위임하고 응답을 반환한다. 비즈니스 로직은 서비스 계층에 두며, 컨트롤러는 요청/응답 처리에만 집중한다.

---

## 2. 원칙

- `@RestController` 선언 필수
- `@Valid`로 입력 검증
- 서비스 위임 후 응답 반환

---

## 3. 강제 사항

### must

- `@RestController` 선언
- `@RequestBody` 파라미터에 `@Valid` 부착
- 비즈니스 로직은 서비스 계층에 위임

---

## 4. 예시 코드

`decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:27-49`

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getMe(@AuthenticationPrincipal String userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
    }

    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_UPDATE_SUCCESS, response));
    }
}
```

Request DTO는 Java `record`로 작성하고 검증 어노테이션을 부착한다.

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
- [ ] `@RequestMapping` 경로 설정
- [ ] `@RequestBody` 파라미터에 `@Valid` 부착
- [ ] 서비스 위임만 수행 (비즈니스 로직 없음)
- [ ] Request DTO는 Java `record` 사용
- [ ] `com.backend.domain.{x}.controller` 패키지에 위치
