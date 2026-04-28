# exception-handling-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 의 예외 처리는 단일 진입점(`GlobalExceptionHandler`) 에서 모든 예외를 포착하고,
`ErrorCode` enum 이 정의한 코드·상태·메시지를 `ErrorResponse` 형태로 반환한다.
도메인별 예외 클래스는 `BusinessException` 을 상속하여 각 도메인 패키지 내 `exception/` 하위에 위치하며,
글로벌 핸들러는 `BusinessException` 단일 핸들러로 모든 도메인 예외를 흡수한다.
검증 실패·보안 예외·미분류 서버 오류는 별도 분기로 처리하고 로그 수준을 4xx/5xx 로 나눠 기록한다.

---

## 원칙

1. 예외 메시지는 `ErrorCode` 에서만 정의한다. 핸들러나 서비스 레이어에서 임의 문자열을 직접 반환하지 않는다.
2. 글로벌 핸들러는 애플리케이션 내 `@RestControllerAdvice` 클래스 1개(`GlobalExceptionHandler`)뿐이다.
3. 도메인 예외는 반드시 `BusinessException` 을 상속하며, 생성자에서 `ErrorCode` 를 고정한다.
4. 4xx 응답은 `log.warn`, 5xx 응답은 `log.error` 로 기록하여 모니터링 노이즈를 최소화한다.
5. 필드 검증 실패(`MethodArgumentNotValidException`, `ConstraintViolationException`)는 `fieldErrors` 맵으로 상세 정보를 포함한다.
6. 보안 예외(`AccessDeniedException`, `AuthenticationException`)는 Spring Security 핸들러(`CustomAccessDeniedHandler`, `CustomAuthenticationEntryPoint`)에서 처리하고, 글로벌 핸들러와 중복 처리하지 않는다.

---

## 강제 사항

### must

- 모든 도메인 예외는 `com.backend.global.error.exception.BusinessException` 을 상속해야 한다.
- `BusinessException` 생성자는 반드시 `ErrorCode` 를 인자로 받아야 한다.
- `ErrorCode` enum 은 `com.backend.global.error.ErrorCode` 단 하나만 존재해야 한다. 도메인별 별도 ErrorCode 파일 생성 금지.
- `@RestControllerAdvice` 클래스는 전체 프로젝트에 1개(`GlobalExceptionHandler`)만 존재해야 한다.
- 응답 객체는 반드시 `ErrorResponse(httpStatus, code, errorMessage, fieldErrors)` 구조를 사용해야 한다.
- 글로벌 핸들러는 최소 6개 분기를 포함해야 한다:
  1. `BusinessException` (도메인 예외 통합)
  2. `MethodArgumentNotValidException` (Bean Validation @RequestBody)
  3. `ConstraintViolationException` (Bean Validation @PathVariable/@RequestParam)
  4. `HttpMessageNotReadableException` (JSON 파싱 실패)
  5. `DataIntegrityViolationException` (DB 제약 위반)
  6. `Exception` (미분류 예외 안전망)
- 4xx 계열 예외는 `log.warn`, 5xx 계열 예외는 `log.error` 로 로깅해야 한다.
- 도메인별 예외 클래스는 도메인 패키지 `exception/` 하위에 위치해야 하며, 이름은 비즈니스 의미를 명확히 드러내야 한다 (`UserNotFoundException`, `DuplicateEmailException` 등).
- `Exception` 핸들러는 반드시 스택트레이스를 포함해 `log.error` 로 기록해야 한다.

### should

- `BindException` 분기를 별도로 두어 form 바인딩 오류도 필드 단위로 응답한다.
- `MissingServletRequestParameterException`, `HttpRequestMethodNotSupportedException`, `NoHandlerFoundException` 분기를 추가하여 클라이언트 오류 원인을 명확히 전달한다.
- `MaxUploadSizeExceededException` 을 별도 분기로 처리하여 파일 업로드 제한을 명시적으로 안내한다.
- `EntityNotFoundException` 분기를 두어 JPA 조회 실패를 일관 처리한다.

---

## 예시 (decapet 인용)

### ErrorCode — 단일 출처 정의

```java
// com.backend.global.error.ErrorCode
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Global
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST,       "G001", "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,  "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,  "U002", "이미 등록된 이메일입니다."),

    // Pet
    PET_NOT_FOUND(HttpStatus.NOT_FOUND,   "P001", "반려동물을 찾을 수 없습니다."),
    // ... (도메인별 코드 추가)

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### BusinessException — 루트 예외

```java
// com.backend.global.error.exception.BusinessException
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### 도메인 예외 클래스 — BusinessException 상속

```java
// com.backend.domain.user.exception.UserNotFoundException
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

// com.backend.domain.user.exception.DuplicateEmailException
public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
```

### GlobalExceptionHandler — 6개 핵심 분기

```java
// com.backend.global.error.GlobalExceptionHandler
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 도메인 예외 (BusinessException 상속 클래스 모두 포착)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
        ErrorResponse response = new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 2. @RequestBody Bean Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        Map<String, String> fieldErrors = extractFieldErrors(e);
        ErrorResponse response = new ErrorResponse(errorCode, fieldErrors);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 3. @PathVariable / @RequestParam Bean Validation 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        // ... fieldErrors 추출 후 반환
    }

    // 4. JSON 역직렬화 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON parse error: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode));
    }

    // 5. DB 제약 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("G007", "데이터 처리 중 오류가 발생했습니다."));
    }

    // 6. 미분류 예외 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode));
    }
}
```

### ErrorResponse — 응답 구조

```java
// com.backend.global.error.ErrorResponse
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final HttpStatus httpStatus;
    private final String code;
    private final String errorMessage;
    private final Map<String, String> fieldErrors;   // 검증 실패 시만 포함
    // ... 생성자 오버로드
}
```

---

## 체크리스트

- [ ] `BusinessException` 상속 없이 `RuntimeException` 을 직접 throw 하는 코드가 없다
- [ ] `ErrorCode` enum 파일이 `com.backend.global.error.ErrorCode` 한 곳뿐이다
- [ ] `@RestControllerAdvice` 클래스가 프로젝트 전체에 1개뿐이다
- [ ] 핸들러에 `BusinessException`, `MethodArgumentNotValidException`, `ConstraintViolationException`, `HttpMessageNotReadableException`, `DataIntegrityViolationException`, `Exception` 분기가 모두 존재한다
- [ ] 4xx 분기는 `log.warn`, 5xx 분기는 `log.error` 를 사용한다
- [ ] `Exception` 핸들러에서 스택트레이스를 `log.error` 로 기록한다
- [ ] 도메인 예외 클래스가 도메인 패키지 내 `exception/` 하위에 위치한다
- [ ] 도메인 예외 클래스 이름이 비즈니스 의미를 명확히 표현한다 (`XxxNotFoundException`, `DuplicateXxxException` 등)
- [ ] `ErrorResponse` 에 `fieldErrors` 가 있고 검증 실패 시 필드별 메시지가 포함된다
- [ ] 보안 예외는 `CustomAccessDeniedHandler` / `CustomAuthenticationEntryPoint` 에서만 처리되고 글로벌 핸들러와 중복되지 않는다
