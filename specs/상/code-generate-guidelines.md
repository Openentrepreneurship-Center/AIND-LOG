# code-generate-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 코드 생성 도구(LLM, IDE 플러그인, 자동화 스크립트)가 `com.backend` 프로젝트에서
코드를 생성·수정할 때 반드시 준수해야 하는 규칙을 정의한다.
규칙의 핵심은 기존 코드를 파괴하지 않는 비파괴 편집, 공개 계약의 보존, 그리고 변경 최소화다.
수동 코드 작성 시에도 동일하게 적용된다.

---

## 2. 핵심 원칙

### 2.1 비파괴 편집 (must)
- 기존 파일을 삭제, 이동, 리네임하지 않는다.
- 새 기능은 신규 파일 또는 기존 파일의 신규 라인·메서드 추가로만 구현한다.
- 기존 코드 블록을 통째로 재생성하여 덮어쓰지 않는다.

### 2.2 public 시그니처 보존 (must)
- 기존 `public` 메서드의 이름, 파라미터 타입·순서, 반환 타입을 변경하지 않는다.
- 기존 `public` 메서드를 삭제하지 않는다.
- 시그니처 변경이 불가피한 경우, 기존 메서드를 유지한 채 오버로드 또는 신규 메서드를 추가한다.

### 2.3 Unified Diff 출력 (must)
- 자동화 도구·LLM이 코드 변경을 제안할 때는 반드시 Unified Diff 형식으로만 출력한다.
- 파일 전체를 재출력하거나 재생성하지 않는다.
- Diff 출력 시 파일 경로와 줄 번호 컨텍스트(`@@`)를 포함한다.

```diff
--- a/src/main/java/com/backend/domain/user/service/UserService.java
+++ b/src/main/java/com/backend/domain/user/service/UserService.java
@@ -50,6 +50,11 @@
     @Transactional(readOnly = true)
     public UserResponse getUser(String userId) {
         User user = userRepository.getByIdAndDeletedAtIsNull(userId);
+        // 추가된 로직
+        if (user.getStatus() == UserStatus.LOCKED) {
+            throw new UserAccountLockedException();
+        }
         return userResponseMapper.toResponse(user);
     }
```

---

## 3. Entity 필드 추가 규칙 (must)

Entity에 새 필드를 추가할 때는 반드시 다음 두 가지를 함께 작성한다.

1. **Entity 필드 선언**: `@Column(nullable = true)` 또는 적절한 nullable 설정 포함
2. **Flyway 마이그레이션 파일**: `src/main/resources/db/migration/V{버전}__{설명}.sql`

```java
// Entity 필드 추가 예시 (nullable 필수)
@Column(nullable = true)
private String profileImageUrl;
```

```sql
-- 대응 Flyway 파일: V3__add_user_profile_image.sql
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500);
```

- 신규 컬럼은 기존 데이터와의 호환성을 위해 `nullable = true` 또는 DEFAULT 값을 포함한다.
- `NOT NULL` 컬럼은 반드시 DEFAULT 절을 포함하거나 데이터 마이그레이션 스크립트를 함께 제공한다.
- Entity와 Flyway 파일이 일치하지 않으면 애플리케이션 기동이 실패한다.

---

## 4. record DTO 변환 금지 (must)

- `record`로 선언된 DTO를 일반 `class`로 변경하지 않는다.
- DTO 내부에 비즈니스 로직이나 변환 메서드를 추가하지 않는다.
- Entity ↔ DTO 변환은 반드시 `@Component` Mapper 클래스에서만 수행한다.

```java
// 올바른 패턴
// decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserMapper.java:13-29
@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
    }
}

// 금지 패턴 — DTO 내부에 변환 로직 추가 금지
public record UserResponse(String id, String email) {
    // public User toEntity() { ... }  ← 금지
}
```

---

## 5. ErrorCode 동시 추가 규칙 (must)

- 새 예외 클래스를 생성할 때는 반드시 `ErrorCode` enum에 대응 항목을 함께 추가한다.
- ErrorCode 항목은 해당 도메인 섹션에 위치하고, 코드 접두사를 도메인 규칙에 맞게 부여한다.

```java
// 1. ErrorCode 추가
// decapet-official/backend/src/main/java/com/backend/global/error/ErrorCode.java
// User 섹션에 추가
NEW_USER_ERROR(HttpStatus.BAD_REQUEST, "U013", "새 오류 메시지"),

// 2. 예외 클래스 생성
// com.backend.domain.user.exception.NewUserException.java
public class NewUserException extends BusinessException {
    public NewUserException() {
        super(ErrorCode.NEW_USER_ERROR);
    }
}
```

- `GlobalExceptionHandler`에는 `BusinessException` 핸들러가 이미 존재하므로, 하위 예외를 별도로 등록하지 않아도 된다.

```java
// decapet-official/backend/src/main/java/com/backend/global/error/GlobalExceptionHandler.java:35-41
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("Business exception: {} - {}", errorCode.getCode(), e.getMessage());
    ErrorResponse response = new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
}
```

---

## 6. 신규 API 엔드포인트 생성 절차

신규 엔드포인트를 추가할 때 아래 순서와 파일을 함께 생성·수정한다.

| 순서 | 파일 | 설명 |
|------|------|------|
| 1 | `dto/request/{Action}{Entity}Request.java` | 입력 record DTO |
| 2 | `dto/response/{Entity}Response.java` | 출력 record DTO (기존 재사용 가능) |
| 3 | `dto/mapper/{Entity}Mapper.java` | 변환 로직 (기존 클래스에 메서드 추가) |
| 4 | `ErrorCode.java` | 새 에러 코드 추가 (필요 시) |
| 5 | `exception/{Name}Exception.java` | 예외 클래스 (필요 시) |
| 6 | `repository/{Entity}Repository.java` | 쿼리 메서드 추가 |
| 7 | `service/{Entity}Service.java` | 비즈니스 로직 메서드 추가 |
| 8 | `controller/{Entity}Api.java` | Swagger 명세 인터페이스에 메서드 추가 |
| 9 | `controller/{Entity}Controller.java` | 실제 구현 메서드 추가 |
| 10 | `db/migration/V{n}__{설명}.sql` | Entity 변경이 있을 경우 |

---

## 7. 금지 행위 목록

- [ ] 기존 파일 삭제·이동·리네임
- [ ] 기존 `public` 메서드 시그니처 변경 또는 삭제
- [ ] Entity 필드 추가 시 Flyway 마이그레이션 파일 미작성
- [ ] `record` DTO를 `class`로 변환
- [ ] DTO 내부에 비즈니스 로직 추가
- [ ] 신규 예외 클래스 생성 시 `ErrorCode` 미추가
- [ ] 기존 트랜잭션 경계(`@Transactional`) 제거 또는 변경
- [ ] 기존 테스트의 어설션을 약화시키는 프로덕션 코드 변경
- [ ] 전체 파일 재생성 방식으로 코드 제안

---

## 8. 체크리스트

- [ ] 기존 파일을 삭제·이동·리네임하지 않았는가
- [ ] 기존 `public` 메서드 시그니처가 그대로 유지되는가
- [ ] 코드 변경이 Unified Diff 형식으로 제안되었는가
- [ ] Entity 필드를 추가했다면 Flyway 마이그레이션 파일이 함께 있는가
- [ ] 신규 컬럼이 `nullable = true` 또는 DEFAULT를 포함하는가
- [ ] `record` DTO가 `class`로 변경되지 않았는가
- [ ] DTO에 변환 로직이 추가되지 않았는가 (Mapper 클래스에 위치하는가)
- [ ] 신규 예외 클래스에 대응하는 `ErrorCode`가 추가되었는가
- [ ] `GlobalExceptionHandler`에 이미 처리 가능한 예외를 중복 등록하지 않았는가
- [ ] 기존 `@Transactional` 경계가 변경되지 않았는가
