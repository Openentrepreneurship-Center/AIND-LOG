# code-generate-guidelines.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 코드(클래스, 메서드, DTO, Entity, Repository)를 추가·수정할 때 따라야 할 규칙을 정의한다.
상 등급의 비파괴 편집, public 시그니처 보존 원칙을 계승한다.
상 등급 원본은 MyBatis XML Mapper 관련 규칙을 포함했으나, 본 프로젝트는 JPA 기반이므로 해당 항목은 JPA 엔티티·Flyway 마이그레이션 규칙으로 대체한다.

## 변경/완화 사항

- 상 등급의 MyBatis XML(select/insert/update/delete, sql fragment) 코드 생성 규칙 → 해당 없음. JPA `@Query` 또는 Spring Data 메서드로 대체
- 상 등급의 Mapper XML 네임스페이스/`<mapper namespace>` 변경 금지 → JPA Repository 인터페이스 시그니처 변경 금지로 대체
- 상 등급의 `[TO_BE_CREATED]` 주석 처리 방식 → 중 등급은 단순 TODO 주석 허용
- record DTO를 새로 추가할 때 기존 클래스(DTO)를 이동·리네임·삭제하지 않는다.

## 이 등급에서 강제하는 것

**must**
- 비파괴 편집: 기존 파일 삭제·리네임·이동 금지. 신규 기능은 신규 파일 또는 신규 라인으로만 추가한다.
- 기존 public 메서드 시그니처(메서드명, 파라미터 타입·순서, 반환 타입) 변경 금지.
- JPA 엔티티에 컬럼 필드를 추가할 때 `nullable = true`(기본값)로 선언하고, 대응하는 Flyway 마이그레이션 스크립트를 함께 작성한다.
- 새로운 record DTO를 추가할 때 기존 DTO 클래스를 record로 변환하거나 이동하지 않는다. 신규 파일로만 추가한다.
- Repository 인터페이스에 default 메서드를 추가할 때 기존 default 메서드의 예외 타입·동작을 변경하지 않는다.

**should**
- LLM 또는 자동화 도구가 코드를 생성할 때 Unified Diff 형식으로 출력하고, 기존 파일 전체를 재생성하지 않는다.
- 기존 테스트의 assertion을 약화시키는 변경을 하지 않는다.
- 도메인 예외를 추가할 때 `ErrorCode` enum에 대응하는 코드를 함께 추가한다.

## JPA 엔티티 필드 추가 규칙

새 컬럼 추가 시 다음 순서를 따른다:

1. Entity 클래스에 필드 추가 (`nullable = true` 기본값)
2. Flyway 마이그레이션 스크립트 작성 (`ALTER TABLE ... ADD COLUMN ... NULL`)
3. 필요한 경우 DTO record 신규 추가 (기존 DTO 수정 최소화)

```java
// com.backend.domain.user.entity.User 에 필드 추가 예시
// 기존 필드 유지, 신규 필드만 추가
@Column(nullable = true)   // 기존 데이터 호환을 위해 nullable 기본
private String profileImageUrl;
```

```sql
-- src/main/resources/db/migration/V{n}__add_profile_image_to_user.sql
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500) NULL;
```

## Repository default 메서드 추가 예시

기존 메서드 변경 없이 신규 default 메서드만 추가 (domain/user/repository/UserRepository.java:39-44 패턴):

```java
// 기존 메서드: 변경 금지
default User getByIdAndDeletedAtIsNull(String id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(UserNotFoundException::new);
}

// 신규 메서드: 아래에 추가
default User getByEmail(String email) {
    return findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
}
```

## GlobalExceptionHandler 핸들러 추가 규칙

기존 핸들러 변경 없이 신규 `@ExceptionHandler`만 추가 (global/error/GlobalExceptionHandler.java 패턴):

```java
// com.backend.global.error.GlobalExceptionHandler
// 기존 핸들러 변경 금지, 신규 예외 핸들러만 추가
@ExceptionHandler(NewDomainException.class)
public ResponseEntity<ErrorResponse> handleNewDomainException(NewDomainException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("New domain exception: {} - {}", errorCode.getCode(), e.getMessage());
    ErrorResponse response = new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), e.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
}
```

## 체크리스트

- [ ] 기존 파일을 삭제·리네임·이동하지 않고 신규 파일 또는 신규 라인으로만 추가하였는가?
- [ ] 기존 public 메서드 시그니처(이름, 파라미터, 반환타입)를 변경하지 않았는가?
- [ ] JPA 엔티티에 필드를 추가할 때 Flyway 마이그레이션 스크립트를 함께 작성하였는가?
- [ ] 새 Entity 필드가 `nullable = true`(기본값)로 선언되었는가?
- [ ] 신규 record DTO를 추가할 때 기존 DTO를 삭제·이동하지 않았는가?
- [ ] 기존 Repository default 메서드의 예외 타입을 변경하지 않았는가?
- [ ] 신규 예외 추가 시 `ErrorCode` enum에 대응 항목을 함께 추가하였는가?
- [ ] 기존 테스트의 assertion을 약화시키는 변경이 없는가?
