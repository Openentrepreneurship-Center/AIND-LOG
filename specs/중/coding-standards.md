# coding-standards.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전반에 적용되는 코딩 원칙과 품질 기준을 정의한다.
생성자 주입, record DTO, 공통 응답·예외 체계를 중심으로 일관된 코드 품질을 유지한다.
신규 파일 작성과 기존 파일 수정 모두에 적용된다.

---

## 2. 설계 원칙

### 2.1 단일 책임 원칙 (should)
각 클래스·메서드는 가능한 한 하나의 책임만 갖도록 설계한다.
Service는 비즈니스 로직, Controller는 HTTP 처리만 담당한다.

### 2.2 개방-폐쇄 원칙 (should)
기존 코드 수정 없이 인터페이스·신규 클래스 추가로 기능을 확장하는 방향을 우선한다.

### 2.3 의존성 역전 원칙 (should)
Service가 Repository 구현체에 직접 의존하지 않도록 인터페이스 타입으로 주입받는다.

---

## 3. 강제 사항

### 3.1 의존성 주입 (must)
- 필드 주입(`@Autowired`)과 수정자 주입은 사용하지 않는다.
- `@RequiredArgsConstructor` + `private final` 생성자 주입만 허용한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:28-32
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
```

### 3.2 DTO는 Java record (must)
- 요청·응답 DTO는 Java `record`로 선언한다.
- DTO 내부에 비즈니스 로직을 두지 않는다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:9
public record PhoneVerifyRequest(
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        String phone,
        String code
) {}
```

### 3.3 공통 응답 (must)
- 성공 응답은 반드시 `SuccessResponse.of(SuccessCode, data)` 형태를 사용한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:38-39
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
```

### 3.4 예외 처리 (must)
- 비즈니스 예외는 `BusinessException` 하위 타입으로 정의하고 `ErrorCode`를 주입한다.
- `GlobalExceptionHandler`가 처리하는 예외를 Controller·Service에서 중복 catch하지 않는다.

### 3.5 @Transactional 명시 (must)
- 조회 메서드에는 `@Transactional(readOnly = true)`, 쓰기 메서드에는 `@Transactional`을 명시한다.

### 3.6 매직 넘버 (should)
- 코드 내 리터럴 숫자·정규식 패턴은 `static final` 상수 또는 `ValidationConstants`로 추출한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:34
private static final int VERIFICATION_TTL_MINUTES = 5;
```

### 3.7 인증 정보 수신 (must)
- Controller에서 인증된 사용자 ID는 `@AuthenticationPrincipal String userId`로만 받는다.

---

## 4. 코드 스타일

- 들여쓰기: 스페이스 4칸
- import 와일드카드(`*`) 금지
- 클래스 최상단에 `static final` 상수, 그 아래에 `private final` 필드 선언
- 주석은 '왜(Why)'에 집중

---

## 5. 체크리스트

- [ ] `@RequiredArgsConstructor` + `private final` 생성자 주입만 사용하는가
- [ ] 요청·응답 DTO가 `record`로 선언되어 있는가
- [ ] 성공 응답이 `SuccessResponse.of(...)` 형태인가
- [ ] 비즈니스 예외가 `BusinessException` 하위 타입이고 `ErrorCode`를 포함하는가
- [ ] 조회 메서드에 `@Transactional(readOnly = true)`가 명시되어 있는가
- [ ] 리터럴 정규식·숫자가 상수로 추출되어 있는가
- [ ] import 와일드카드가 없는가
