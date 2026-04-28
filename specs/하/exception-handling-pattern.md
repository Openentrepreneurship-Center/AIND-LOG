# exception-handling-pattern.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 예외 처리의 최소 요건을 정의한다.
글로벌 핸들러 1개(`GlobalExceptionHandler`)를 통해 모든 예외를 처리하며, 사용자에게 명확하고 정중한 메시지를 반환하는 것이 핵심 목표다.
`BusinessException` 활용을 권장하나, `RuntimeException` 직접 throw 도 허용한다.
단, 사용자에게 노출되는 메시지는 항상 한국어로, 내부 스택트레이스를 그대로 반환하지 않아야 한다.

---

## 2. 변경/완화 사항

| 항목 | 중 등급 원칙 | 하 등급 완화 |
|------|------------|------------|
| 예외 throw 방식 | `BusinessException` + `ErrorCode` 필수 | `RuntimeException` 직접 throw 허용 |
| 글로벌 핸들러 | `@RestControllerAdvice` 1개 강제 | 동일 (완화 없음) |
| 로그 레벨 구분 | 4xx warn / 5xx error 강제 | 구분 없이 `log.warn` 또는 `log.error` 중 택일 허용 |
| 필드별 오류 map | `MethodArgumentNotValidException` 시 필드 map 필수 | 단순 메시지 반환 허용 |
| `ErrorCode` 도메인 접두사 | 패턴 강제 (`U001`, `AP012` 등) | 신규 코드 추가 시 기존 코드 패턴 유지 권고 (강제 아님) |

---

## 3. 강제 사항

### must

- `@RestControllerAdvice`를 선언한 글로벌 핸들러가 프로젝트에 반드시 1개 존재해야 한다.
- 처리되지 않은 예외(`Exception.class`)가 사용자에게 스택트레이스 그대로 노출되어서는 안 된다.
- 예외 발생 시 HTTP 상태 코드는 의미에 맞게 설정해야 한다 (예: 찾을 수 없음 → 404, 권한 없음 → 403).
- 사용자 응답 메시지는 한국어로, 기술적 내부 정보를 포함하지 않아야 한다.

### should

- 가능하면 `BusinessException` + `ErrorCode`를 활용하여 중 등급 패턴으로 개선한다.
- 예외 발생 시 최소한 `log.warn` 또는 `log.error`로 서버 로그에 기록한다.

---

## 4. 예시 코드

### 4.1 최소 구현 — 글로벌 핸들러

```java
// com.backend.global.error.GlobalExceptionHandler
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BusinessException 처리 (권장 경로)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
            .body(new ErrorResponse(errorCode));
    }

    // 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation failed");
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("G001", "요청 값이 유효하지 않습니다."));
    }

    // 그 외 모든 예외 — 내부 정보 노출 금지
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("G002", "서버 오류가 발생했습니다."));
    }
}
```

### 4.2 RuntimeException 직접 throw (허용 — 단, 사용자 메시지 정중하게)

```java
// 하 등급에서 허용되는 패턴
public User findById(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
}

// 권장 패턴 (BusinessException 사용)
public User findById(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
}
```

---

## 5. 체크리스트

- [ ] `@RestControllerAdvice` 핸들러가 프로젝트에 1개 존재하는가?
- [ ] 처리되지 않은 예외가 스택트레이스 그대로 사용자에게 노출되지 않는가?
- [ ] 예외 응답의 HTTP 상태 코드가 의미에 맞게 설정되어 있는가?
- [ ] 사용자 응답 메시지가 한국어이고 내부 기술 정보를 포함하지 않는가?
- [ ] 예외 발생 시 서버 로그에 최소한 1건 이상 기록되는가?
