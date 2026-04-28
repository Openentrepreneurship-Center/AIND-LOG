# exception-handling-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 의 예외 처리는 `BusinessException` 계층, `ErrorCode` enum 단일 출처,
`@RestControllerAdvice` 1개(`GlobalExceptionHandler`) 라는 세 축으로 운영된다.
도메인에서 발생하는 비즈니스 오류는 `BusinessException` 을 상속한 도메인 전용 클래스로 표현하고,
핸들러가 이를 일괄 포착하여 `ErrorResponse` 로 변환한다.
검증 실패 및 서버 오류는 별도 분기에서 처리하며 로그 수준을 4xx/5xx 로 구분한다.

---

## 원칙

1. 비즈니스 예외는 반드시 `BusinessException` 을 상속한다. `RuntimeException` 직접 사용 금지.
2. 에러 코드·상태·메시지는 `ErrorCode` enum 한 곳에서만 정의한다.
3. `@RestControllerAdvice` 클래스는 프로젝트 전체에 1개만 존재한다.
4. 4xx 는 `log.warn`, 5xx 는 `log.error` 로 기록한다.
5. 응답 형식은 `ErrorResponse(httpStatus, code, errorMessage, fieldErrors)` 를 사용한다.

---

## 강제 사항

### must

- 도메인 예외는 `com.backend.global.error.exception.BusinessException` 을 상속해야 한다.
- `ErrorCode` 는 `com.backend.global.error.ErrorCode` 단 하나여야 한다.
- `@RestControllerAdvice` 클래스는 1개(`GlobalExceptionHandler`)만 존재해야 한다.
- 글로벌 핸들러에 다음 4개 이상의 분기가 있어야 한다:
  1. `BusinessException`
  2. `MethodArgumentNotValidException`
  3. `ConstraintViolationException`
  4. `Exception` (안전망)
- 4xx 분기는 `log.warn`, 5xx 분기는 `log.error` 로 기록해야 한다.

### should

- `HttpMessageNotReadableException`, `DataIntegrityViolationException` 분기를 추가하여 클라이언트 오류를 더 세밀하게 처리한다.
- 검증 실패 응답에 `fieldErrors` 맵으로 필드별 오류 메시지를 포함한다.

---

## 예시 (decapet 인용)

### ErrorCode — 단일 출처

```java
// com.backend.global.error.ErrorCode
@Getter
@AllArgsConstructor
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G001", "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 오류가 발생했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 등록된 이메일입니다."),
    // ...

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### BusinessException 상속 — 도메인 예외

```java
// com.backend.domain.user.exception.UserNotFoundException
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

### GlobalExceptionHandler — 4개 핵심 분기

```java
// com.backend.global.error.GlobalExceptionHandler
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        // fieldErrors 추출 후 반환
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode, extractFieldErrors(e)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        // fieldErrors 추출 후 반환
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode));
    }
}
```

---

## 체크리스트

- [ ] `BusinessException` 없이 `RuntimeException` 을 직접 throw 하는 코드가 없다
- [ ] `ErrorCode` 파일이 `com.backend.global.error.ErrorCode` 한 곳뿐이다
- [ ] `@RestControllerAdvice` 클래스가 1개뿐이다
- [ ] 핸들러에 최소 4개 분기(`BusinessException`, `MethodArgumentNotValidException`, `ConstraintViolationException`, `Exception`)가 있다
- [ ] 4xx 분기에 `log.warn`, 5xx 분기에 `log.error` 가 사용된다
- [ ] 검증 실패 응답에 `fieldErrors` 맵이 포함된다
- [ ] 도메인 예외 클래스가 `exception/` 하위에 위치하고 이름이 비즈니스 의미를 드러낸다
