# exception-handling-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 `@RestControllerAdvice` 클래스 1개(`GlobalExceptionHandler`)에서
모든 예외를 포착하여 일관된 형식으로 응답한다.
비즈니스 오류는 예외로 throw 하고, 핸들러가 이를 포착해 사용자에게 정중한 메시지를 전달한다.

---

## 원칙

1. 예외는 서비스 레이어에서 throw 하고, 핸들러에서 포착한다. 컨트롤러에서 직접 오류 메시지를 구성하지 않는다.
2. 글로벌 핸들러는 프로젝트 전체에 1개만 존재한다.
3. 사용자에게 전달하는 메시지는 기술 세부 사항 없이 정중하고 간결하게 작성한다.

---

## 강제 사항

### must

- `@RestControllerAdvice` 클래스가 1개 존재해야 한다.
- 비즈니스 오류는 예외를 throw 하여 핸들러에서 처리해야 한다. 컨트롤러에서 오류 응답을 직접 반환 금지.
- 미분류 예외(`Exception`)를 포착하는 안전망 핸들러가 있어야 한다.
- 응답 메시지는 사용자가 이해할 수 있는 한국어 문장이어야 한다.

---

## 예시 (decapet 인용)

### 글로벌 핸들러 기본 구조

```java
// com.backend.global.error.GlobalExceptionHandler
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage()));
    }

    // 미분류 예외 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("G002", "서버 오류가 발생했습니다."));
    }
}
```

### 도메인 예외 throw 예시

```java
// com.backend.domain.user.service.UserService (예시)
public UserResponse getUser(String userId) {
    return userRepository.findById(userId)
            .map(userResponseMapper::toResponse)
            .orElseThrow(UserNotFoundException::new);  // BusinessException 상속
}
```

---

## 체크리스트

- [ ] `@RestControllerAdvice` 클래스가 1개뿐이다
- [ ] 비즈니스 오류가 예외로 throw 되고 핸들러에서 포착된다
- [ ] 미분류 예외 안전망(`Exception` 핸들러)이 있다
- [ ] 사용자 응답 메시지가 한국어로 정중하게 작성되어 있다
- [ ] 컨트롤러가 오류 응답을 직접 구성하지 않는다
