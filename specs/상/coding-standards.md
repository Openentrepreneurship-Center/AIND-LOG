# coding-standards.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지 전반에 걸쳐 적용되는 코딩 원칙과 품질 기준을 정의한다.
객체지향 설계 5원칙(SOLID)을 기반으로 하며, 확장 가능하고 유지보수하기 쉬운 코드 작성을 목표로 한다.
모든 개발자는 신규 파일 작성 및 기존 파일 수정 시 본 기준을 준수해야 한다.
예외 없이 적용되며, 자동화된 리뷰 도구와 코드 생성 도구도 동일한 원칙을 따른다.

---

## 2. 설계 원칙 (SOLID — 모두 must)

### 2.1 단일 책임 원칙 (SRP)
- 각 클래스·메서드는 하나의 명확한 책임만 가진다.
- Service 클래스는 비즈니스 로직만 담당하고, 입력 검증·HTTP 처리는 Controller 계층에 위임한다.
- 변경 이유가 두 가지 이상이면 클래스 분리를 검토한다.

### 2.2 개방-폐쇄 원칙 (OCP)
- 기존 코드를 수정하지 않고 확장할 수 있도록 인터페이스와 추상화를 활용한다.
- 새 기능은 기존 메서드 변경이 아닌 신규 메서드·클래스 추가로 구현한다.

### 2.3 리스코프 대체 원칙 (LSP)
- 하위 타입은 상위 타입의 계약을 완전히 준수해야 한다.
- `BusinessException` 하위 예외는 `ErrorCode`를 반드시 주입하고 동작 계약을 유지한다.

### 2.4 인터페이스 분리 원칙 (ISP)
- 클라이언트가 사용하지 않는 메서드를 강제로 구현하게 해선 안 된다.
- Controller의 Swagger 명세(`*Api` 인터페이스)와 실제 구현을 분리해 각자의 책임을 명확히 한다.

### 2.5 의존성 역전 원칙 (DIP)
- 고수준 모듈(Service)은 저수준 모듈(Repository 구현체)에 직접 의존하지 않는다.
- 스프링의 의존성 주입과 인터페이스 타입을 통해 결합도를 낮춘다.

---

## 3. 강제 사항

### 3.1 의존성 주입 (must)
- 필드 주입(`@Autowired`)과 수정자 주입은 금지한다.
- 생성자 주입을 유일한 주입 방식으로 사용하며, Lombok `@RequiredArgsConstructor`로 선언한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:28-32
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
```

### 3.2 필드 선언 위치 (must)
- 모든 `private final` 필드는 클래스 최상단에 선언한다.
- `static final` 상수도 필드 블록 최상단(인스턴스 필드 위)에 위치한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:34
private static final int VERIFICATION_TTL_MINUTES = 5;
```

### 3.3 import fullpath 사용 금지 (must)
- 코드 본문에서 `com.backend.global.error.ErrorCode.VALIDATION_FAILED` 같은 완전 경로 타입 직접 사용은 금지한다.
- import 문을 반드시 추가하고 단순 타입명으로 참조한다.
- IDE 자동 import 정렬을 따르며, 와일드카드(`*`) import는 금지한다.

### 3.4 DTO는 Java record (must)
- 요청·응답 DTO는 모두 Java `record`로 선언한다.
- `record` 내부에는 비즈니스 로직을 두지 않는다. 변환 로직은 Mapper(`@Component`)에 위임한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/request/PhoneVerifyRequest.java:9-19
public record PhoneVerifyRequest(
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        String phone,

        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {
}
```

### 3.5 Entity ↔ DTO 변환 (must)
- Entity를 직접 Controller 반환값으로 사용하지 않는다.
- 변환은 반드시 `@Component` Mapper 클래스에서 수행한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserMapper.java:13-14
@Component
@RequiredArgsConstructor
public class UserMapper {
```

### 3.6 공통 응답 (must)
- 성공 응답은 반드시 `SuccessResponse.of(SuccessCode, data)` 형태로 반환한다.
- 직접 도메인 객체를 `ResponseEntity` body에 넣지 않는다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:38-39
UserResponse response = userService.getUser(userId);
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
```

### 3.7 매직 넘버 상수화 (must)
- 코드 내 리터럴 숫자·문자열은 `static final` 상수 또는 `ValidationConstants`·`ErrorCode` 같은 공용 상수 클래스로 추출한다.
- 정규식 패턴은 반드시 `ValidationConstants`에 위치시킨다.

```java
// decapet-official/backend/src/main/java/com/backend/global/common/constants/ValidationConstants.java:9-10
public static final String PHONE_REGEX = "^010\\d{8}$";
public static final String PHONE_MESSAGE = "전화번호 형식이 올바르지 않습니다. (010XXXXXXXX)";
```

### 3.8 public 메서드 길이 (should)
- public 메서드는 50줄을 초과하지 않도록 한다. 초과 시 private 헬퍼 메서드로 분리한다.
- 단일 메서드가 3단계 이상 중첩 블록을 가지면 추출을 검토한다.

### 3.9 테스트 가능성 (must)
- 외부 의존성(SMS 발송, S3, AWS SNS 등)은 인터페이스·추상화를 통해 주입받아야 한다.
- `static` 유틸 메서드는 순수 함수(입력 → 출력, 부수효과 없음)로만 작성한다.
- `new` 키워드로 외부 시스템 클라이언트를 직접 생성하지 않는다.

### 3.10 예외 처리 (must)
- 비즈니스 예외는 반드시 `BusinessException` 하위 타입으로 정의하고, `ErrorCode`를 주입한다.
- `GlobalExceptionHandler`에 이미 정의된 예외 유형에 대해 개별 try-catch를 Controller·Service에 두지 않는다.
- `Exception`을 직접 catch하는 코드는 금지한다(GlobalExceptionHandler에서 최후 처리).

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/exception/UserNotFoundException.java
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

### 3.11 @Transactional 사용 (must)
- 읽기 전용 조회 메서드에는 `@Transactional(readOnly = true)`를 명시한다.
- 쓰기 트랜잭션이 필요한 메서드에는 `@Transactional`을 명시한다.
- Service 클래스 레벨에 `@Transactional`을 두지 않고, 메서드 단위로만 선언한다.

### 3.12 인증 정보 수신 (must)
- Controller에서 인증된 사용자 ID는 `@AuthenticationPrincipal String userId`로만 받는다.
- HttpServletRequest에서 직접 토큰을 파싱하는 코드를 Controller에 두지 않는다.

---

## 4. 코드 스타일

- 들여쓰기: 스페이스 4칸
- 중괄호: K&R 스타일 (여는 중괄호는 같은 줄)
- 줄 끝 공백 금지
- 파일 끝 개행 문자 1개 유지
- 주석은 '왜(Why)'에 집중하고, '무엇(What)'은 코드가 설명하게 한다

---

## 5. 체크리스트

코드 작성·수정 시 아래 항목을 하나씩 확인한다.

- [ ] `@RequiredArgsConstructor` + `private final` 생성자 주입만 사용하는가
- [ ] 클래스 최상단에 `static final` 상수, 그 아래에 `private final` 필드가 선언되어 있는가
- [ ] `import` 문 없이 완전 경로 타입을 코드 본문에 직접 쓰지 않았는가
- [ ] 요청·응답 DTO가 모두 Java `record`로 선언되어 있는가
- [ ] Entity가 Controller 반환값으로 직접 노출되지 않는가
- [ ] 성공 응답이 `SuccessResponse.of(...)` 형태인가
- [ ] 코드 내 리터럴 숫자·패턴이 상수로 추출되어 있는가
- [ ] 비즈니스 예외가 `BusinessException` 하위 타입으로 정의되고 `ErrorCode`를 포함하는가
- [ ] 조회 메서드에 `@Transactional(readOnly = true)`가 명시되어 있는가
- [ ] public 메서드가 50줄을 초과하지 않는가
- [ ] 외부 시스템 의존성이 인터페이스를 통해 주입받고 있는가
- [ ] SOLID 5원칙 중 위반 항목이 없는가
