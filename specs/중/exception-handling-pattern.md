# exception-handling-pattern.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 `com.backend` 패키지 기반 decapet-official 백엔드의 예외 처리 표준을 정의한다.
모든 비즈니스 예외는 `BusinessException`을 상속하고, `ErrorCode` enum 에 등록된 코드를 인자로 사용한다.
예외 처리 진입점은 `GlobalExceptionHandler` 하나뿐이며, 응답 포맷은 `ErrorResponse`로 통일한다.
4xx 예외는 `warn`, 5xx 예외는 `error` 레벨로 로깅한다.
서비스 레이어에서 `RuntimeException`을 직접 throw 하는 것은 금지한다.

---

## 2. 변경/완화 사항

| 항목 | 상 등급 원칙 | 중 등급 적용 |
|------|------------|------------|
| 예외 베이스 클래스 | `BizException` (외부 정의) | `BusinessException` (`com.backend.global.error.exception`) |
| 에러 코드 enum | `BizErrorCode` (외부 정의) | `ErrorCode` (status, code, message 3필드) |
| 응답 포맷 | 별도 명시 없음 | `ErrorResponse` 단일 포맷 (`com.backend.global.error`) |
| WebClient 예외 분리 | `CustomWebClientException` / `CustomWebServerException` 별도 | 외부 요청 예외도 `BusinessException` + 적절한 `ErrorCode`로 통합 허용 |
| 로그 레벨 구분 | 명시 없음 | 4xx warn / 5xx error 강제 |

---

## 3. 강제 사항

### must (위반 시 코드 리뷰 반려)

- 도메인 예외 클래스는 반드시 `BusinessException`을 상속해야 한다.
- `BusinessException` 생성 시 반드시 `ErrorCode` enum 인스턴스를 인자로 전달해야 한다.
- `ErrorCode` 추가 시 (HttpStatus, 코드 문자열, 메시지) 3필드를 모두 채워야 한다.
- 코드 문자열은 도메인 접두사 + 3자리 숫자 패턴을 유지해야 한다 (예: `U001`, `AP012`).
- 서비스 레이어에서 `new RuntimeException(...)`, `new IllegalArgumentException(...)` 등 비커스텀 예외를 직접 throw 해서는 안 된다.
- `GlobalExceptionHandler` 이외의 클래스에 `@ExceptionHandler`를 선언해서는 안 된다.
- `MethodArgumentNotValidException` 핸들러는 필드별 오류를 `Map<String, String>` 형태로 응답 본문에 포함해야 한다.
- `@RestControllerAdvice`는 프로젝트 전체에서 1개(`GlobalExceptionHandler`)만 존재해야 한다.

### should (팀 합의 후 예외 허용)

- 도메인별 커스텀 예외 클래스 (`UserNotFoundException` 등)를 만들 경우 `BusinessException`을 상속하고, 생성자 내부에서 고정 `ErrorCode`를 super에 전달하는 패턴을 권장한다.
- 로그 메시지에는 `ErrorCode.code` 값을 포함시켜 운영 시 빠른 식별이 가능하도록 한다.
- 외부 시스템(PG사, SMS 등) 연동 실패 시에는 전용 `ErrorCode`를 등록하고 서비스에서 `BusinessException`으로 감싸서 던진다.

---

## 4. 예시 코드

### 4.1 ErrorCode 등록 패턴

```java
// com.backend.global.error.ErrorCode
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G001", "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 등록된 이메일입니다."),

    // Pet
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "반려동물을 찾을 수 없습니다."),
    PET_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "P005", "해당 반려동물에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### 4.2 BusinessException 사용 패턴

```java
// 서비스에서 BusinessException throw — 직접 ErrorCode 전달
public User findById(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
}

// 추가 메시지가 필요한 경우 두 번째 인자 활용
public void validateOwnership(String petId, String userId) {
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
    if (!pet.isOwnedBy(userId)) {
        throw new BusinessException(ErrorCode.PET_PERMISSION_DENIED,
            "petId=" + petId + " 접근 거부");
    }
}
```

### 4.3 도메인 커스텀 예외 클래스 (선택)

```java
// com.backend.domain.user.exception.UserNotFoundException
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

### 4.4 GlobalExceptionHandler 핵심 분기

```java
// com.backend.global.error.GlobalExceptionHandler (실제 코드 발췌)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 4xx — warn 레벨
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
        ErrorResponse response = new ErrorResponse(
            errorCode.getStatus(), errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 유효성 검증 실패 — 필드 오류 map 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse response = new ErrorResponse(errorCode, fieldErrors);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 5xx — error 레벨
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
```

---

## 5. 체크리스트

- [ ] 서비스 레이어에서 `RuntimeException` / `IllegalArgumentException` 직접 throw 가 없는가?
- [ ] 새로 추가한 예외 클래스가 `BusinessException`을 상속하는가?
- [ ] `BusinessException` 생성자 인자로 `ErrorCode` enum 값이 전달되는가?
- [ ] 신규 `ErrorCode` 항목에 (HttpStatus, code 문자열, message) 3필드가 모두 채워져 있는가?
- [ ] 코드 문자열이 기존 도메인 접두사 패턴(`U`, `P`, `AP`, `PM` 등)을 따르는가?
- [ ] `@RestControllerAdvice` 어노테이션이 `GlobalExceptionHandler` 이외에 선언되어 있지 않은가?
- [ ] 유효성 검증 실패 응답에 필드별 오류 `Map`이 포함되어 있는가?
- [ ] 4xx 예외는 `log.warn`, 5xx 예외는 `log.error`로 로깅하는가?
- [ ] `DataIntegrityViolationException` 등 JPA 인프라 예외가 `GlobalExceptionHandler`에서 처리되는가?
- [ ] 외부 연동 실패 예외에 전용 `ErrorCode`가 등록되어 있는가?
