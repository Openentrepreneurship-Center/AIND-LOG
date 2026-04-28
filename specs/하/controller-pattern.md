# controller-pattern.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 최소한의 강제 규칙만 정의한다.
`@RestController`와 입력 `@Valid` 검증은 필수이며, 그 외 응답 형식, Swagger 어노테이션, 로깅은 권장 수준으로 완화한다.
한 도메인에 컨트롤러 1개 운영을 권장하나, 강제하지 않는다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상/중 등급 | 하 등급 |
|------|-----------|---------|
| `ResponseEntity<SuccessResponse>` 반환 | 필수 | 자율 (`ResponseEntity<?>` 또는 직접 객체 반환 허용) |
| `@AuthenticationPrincipal String userId` | 필수 | 권장 (다른 인증 방식 허용) |
| Swagger `@Operation` | 권장/필수 | 선택 |
| `@Slf4j` 로깅 | 권장 | 선택 |
| `*Api` 인터페이스 분리 | 권장 | 선택 |

---

## 3. 강제 사항

### must
- `@RestController` 선언
- 요청 DTO에 `@Valid` 적용
- 컨트롤러 내 비즈니스 로직 직접 구현 금지

### should
- `@RequestMapping("/api/v1/{domain}")` URL 패턴 준수
- 응답은 `ResponseEntity<SuccessResponse>` 사용
- 인증 사용자 식별에 `@AuthenticationPrincipal String userId` 사용

---

## 4. 예시 코드

최소 구현 예시 (decapet `UserController` 패턴 참고):

```java
// decapet-official/backend/.../user/controller/UserController.java:27-39
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getMe(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS,
                userService.getUser(userId)));
    }
}
```

---

## 5. 체크리스트

- [ ] `@RestController` 선언되었는가
- [ ] 요청 DTO에 `@Valid`가 적용되었는가
- [ ] 컨트롤러 내에 비즈니스 로직이 없는가
- [ ] URL 패턴이 `/api/v1/{domain}` 형식에 가까운가
- [ ] 서비스 계층에 위임하는 구조인가
