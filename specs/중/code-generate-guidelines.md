# code-generate-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 코드 생성 도구가 `com.backend` 프로젝트에서 코드를 생성·수정할 때
준수해야 하는 규칙을 정의한다.
기존 코드를 파괴하지 않는 비파괴 편집과 공개 계약 보존이 핵심이다.

---

## 2. 핵심 원칙

### 2.1 비파괴 편집 (must)
- 기존 파일을 삭제, 이동, 리네임하지 않는다.
- 새 기능은 신규 파일 또는 기존 파일에 신규 메서드·라인을 추가하는 방식으로만 구현한다.

### 2.2 public 시그니처 보존 (must)
- 기존 `public` 메서드의 이름, 파라미터, 반환 타입을 변경하지 않는다.
- 시그니처 변경이 필요하면 신규 오버로드 메서드를 추가하고 기존 메서드를 유지한다.

### 2.3 Unified Diff 출력 (must)
- 코드 변경 제안은 Unified Diff 형식으로만 출력한다.
- 파일 전체를 재생성하는 방식을 사용하지 않는다.

---

## 3. Entity 필드 추가 규칙 (must)

Entity에 새 필드를 추가할 때 Flyway 마이그레이션 파일을 반드시 함께 작성한다.

```java
// Entity 필드 선언 예시 (nullable 포함)
@Column(nullable = true)
private String profileImageUrl;
```

```sql
-- V3__add_user_profile_image.sql
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500);
```

---

## 4. ErrorCode 동시 추가 (must)

새 예외 클래스를 생성할 때 `ErrorCode` enum에 대응 항목을 함께 추가한다.

```java
// decapet-official/backend/src/main/java/com/backend/global/error/ErrorCode.java 에 추가
NEW_ERROR(HttpStatus.BAD_REQUEST, "U013", "오류 메시지"),

// 예외 클래스
public class NewException extends BusinessException {
    public NewException() {
        super(ErrorCode.NEW_ERROR);
    }
}
```

`GlobalExceptionHandler`의 `BusinessException` 핸들러가 하위 예외를 자동 처리하므로
개별 `@ExceptionHandler`를 추가하지 않아도 된다.

```java
// decapet-official/backend/src/main/java/com/backend/global/error/GlobalExceptionHandler.java:35-41
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus()).body(
        new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage()));
}
```

---

## 5. 금지 행위

- [ ] 기존 파일 삭제·이동·리네임
- [ ] 기존 `public` 메서드 시그니처 변경 또는 삭제
- [ ] Entity 필드 추가 시 Flyway 파일 미작성
- [ ] `record` DTO를 `class`로 변환
- [ ] 신규 예외 생성 시 `ErrorCode` 미추가
- [ ] 기존 `@Transactional` 경계 제거 또는 변경

---

## 6. 체크리스트

- [ ] 기존 파일을 삭제·이동·리네임하지 않았는가
- [ ] 기존 `public` 메서드 시그니처가 유지되는가
- [ ] 코드 변경이 Unified Diff 형식으로 제안되었는가
- [ ] Entity 필드 추가 시 Flyway 마이그레이션 파일이 함께 있는가
- [ ] 신규 예외에 대응하는 `ErrorCode`가 추가되었는가
- [ ] `record` DTO가 `class`로 변경되지 않았는가
- [ ] 기존 `@Transactional` 경계가 변경되지 않았는가
