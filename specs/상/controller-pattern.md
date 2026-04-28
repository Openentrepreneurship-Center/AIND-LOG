# controller-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

컨트롤러 계층은 HTTP 요청을 수신하고 서비스 계층에 처리를 위임한 뒤 공통 응답 형식으로 반환하는 역할만 수행한다. 비즈니스 로직, 데이터 변환, 예외 판단은 컨트롤러에 두지 않는다.

본 프로젝트의 공통 응답은 `SuccessResponse`(com.backend.global.common.SuccessResponse), 공통 예외는 `GlobalExceptionHandler`가 일괄 처리한다. 인증은 JWT 쿠키 기반이며 인증된 사용자 식별자는 `@AuthenticationPrincipal String userId`로 주입받는다.

---

## 2. 원칙 / 패턴 설명

### 2.1 클래스 구조 분리 — Controller + Api 인터페이스

Swagger 어노테이션이 컨트롤러 본문을 오염시키지 않도록 **별도 인터페이스(`*Api`)에 OpenAPI 명세를 작성**하고, 컨트롤러는 해당 인터페이스를 구현한다.

```
com.backend.domain.user.controller
├── UserApi.java          // @Tag, @Operation, @ApiResponses 전담
└── UserController.java   // @RestController, 실제 메서드 구현
```

### 2.2 URL 규칙

- 클래스 레벨: `@RequestMapping("/api/v1/{domain}s")`
- 리소스 단수형 경로 변수: `/{id}`, `/{userId}`
- 하위 자원: `/me/phone/sms/send`, `/me/password`

### 2.3 응답 형식

모든 성공 응답은 `ResponseEntity<SuccessResponse>`로 반환한다.

```java
// 데이터 있음
ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));

// 데이터 없음 (탈퇴 등)
ResponseEntity.ok()
    .headers(cookieUtil.createTokenClearHeaders())
    .body(SuccessResponse.of(SuccessCode.USER_DELETE_SUCCESS));
```

### 2.4 위임 원칙

메서드 본문은 **서비스 호출 1줄 + 응답 반환 1줄** 이내를 원칙으로 한다. 변환·조건 분기·예외 throw는 허용하지 않는다.

### 2.5 PII 로깅 금지

이름, 전화번호, 이메일 등 개인식별정보를 로그로 출력할 때는 반드시 마스킹 처리한다. 원본 값을 `log.info`, `log.debug` 등에 직접 삽입하지 않는다.

---

## 3. 강제 사항

### must (반드시 준수)

| 항목 | 내용 |
|------|------|
| `@RestController` | 모든 컨트롤러 클래스에 선언 |
| `@RequestMapping("/api/v1/{domain}")` | 클래스 레벨에 기본 경로 명시 |
| `@RequiredArgsConstructor` | 생성자 주입 전용. 필드 주입(`@Autowired`) 사용 금지 |
| `@Valid` | `@RequestBody` DTO 파라미터에 항상 부착 |
| `@AuthenticationPrincipal String userId` | 인증 사용자 식별자는 반드시 이 방식으로 수신 |
| `ResponseEntity<SuccessResponse>` | 모든 엔드포인트의 반환 타입 |
| Swagger `@Operation` + `@ApiResponses` | `*Api` 인터페이스에 모든 엔드포인트에 대해 작성 |
| `@SecurityRequirement(name = "cookieAuth")` | 인증 필요 엔드포인트에 명시 |
| HTTP 상태코드 명시 | `@ApiResponse(responseCode = "200/400/401/404/409/...)` |
| PII 마스킹 | 로그 출력 시 개인식별정보 마스킹 필수 |
| 컨트롤러 비즈니스 로직 금지 | 서비스 호출 외 로직 금지 |

### should (권장)

- 메서드별 `@ResponseStatus`로 예상 HTTP 상태코드를 문서화한다.
- `@ExampleObject`를 통해 응답 예시 JSON을 `*Api` 인터페이스에 포함한다.

---

## 4. 예시 코드

### 4.1 Api 인터페이스 (Swagger 명세 전담)

`decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserApi.java:26-101`

```java
@Tag(name = "사용자", description = "프로필 관리")
public interface UserApi {

    @Operation(summary = "내 정보 조회", description = "...")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "code": "U001",
                      "httpStatus": "OK",
                      "message": "회원 정보 조회 성공",
                      "data": { "id": "...", "email": "hong@example.com" }
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse> getMe(@Parameter(hidden = true) String userId);
}
```

### 4.2 Controller 구현체

`decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:27-90`

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
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse> deleteAccount(@AuthenticationPrincipal String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok()
                .headers(cookieUtil.createTokenClearHeaders())
                .body(SuccessResponse.of(SuccessCode.USER_DELETE_SUCCESS));
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

### 4.3 Request DTO — record + ValidationConstants

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:1-20`

```java
@Schema(description = "연락처 변경 SMS 인증 요청")
public record PhoneVerifyRequest(
    @Schema(description = "새 전화번호 (010으로 시작하는 11자리)", example = "01012345678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
    String phone,

    @Schema(description = "인증번호 (6자리)", example = "123456")
    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
    String code
) {}
```

### 4.4 공통 응답 구조

`decapet-official/backend/src/main/java/com/backend/global/common/SuccessResponse.java:1-27`

```java
// SuccessResponse.of(SuccessCode, data) 또는 SuccessResponse.of(SuccessCode) 사용
public class SuccessResponse {
    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
    private final Object data;
}
```

---

## 5. 체크리스트

### 클래스 선언
- [ ] `@RestController` 선언
- [ ] 클래스 레벨 `@RequestMapping("/api/v1/{domain}")` 설정
- [ ] `@RequiredArgsConstructor` 선언 (필드 주입 없음)
- [ ] `*Api` 인터페이스 implements

### 메서드 구현
- [ ] 모든 `@RequestBody` 파라미터에 `@Valid` 부착
- [ ] 인증 필요 메서드에 `@AuthenticationPrincipal String userId` 선언
- [ ] 반환 타입이 `ResponseEntity<SuccessResponse>`
- [ ] 서비스 호출 + 응답 반환만 포함 (비즈니스 로직 없음)

### Swagger 명세 (`*Api` 인터페이스)
- [ ] `@Tag` 클래스 레벨 선언
- [ ] 모든 엔드포인트에 `@Operation(summary, description)` 작성
- [ ] 모든 엔드포인트에 `@ApiResponses` (성공/실패 케이스 모두)
- [ ] `@SecurityRequirement(name = "cookieAuth")` 인증 필요 메서드에 부착
- [ ] 응답 예시 `@ExampleObject` 포함

### 보안
- [ ] 로그 출력 시 이름/전화번호/이메일 마스킹 처리
- [ ] 컨트롤러 레이어에 DB 접근 코드 없음
- [ ] `@AuthenticationPrincipal` 외 토큰 직접 파싱 없음

### 요청 DTO
- [ ] Java `record` 사용
- [ ] 검증 어노테이션 (`@NotBlank`, `@Pattern` 등) 적용
- [ ] 정규식은 `ValidationConstants` 상수 참조
- [ ] `@Schema` 필드 설명 및 example 작성
