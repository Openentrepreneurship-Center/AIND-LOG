# quality-rule.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 decapet-official/backend 프로젝트의 JPA 기반 품질 규칙을 정의한다.
상 등급이 MyBatis `#{}` / ResultMap 중심이었다면, 본 프로젝트는 JPA 파라미터 바인딩과
Specification 패턴으로 동일한 보안 수준을 달성한다.
Controller → Service → Repository 계층별 필수 규칙을 명시하며,
엔티티 직접 노출 금지와 Soft Delete 정책을 전 계층에서 일관되게 적용한다.

---

## 2. 변경/완화 사항

| 상 등급 항목 | 중 등급 변경 내용 |
|---|---|
| MyBatis `#{}` 바인딩 | JPA `@Param` + JPQL 파라미터 바인딩으로 대체 |
| ResultMap 으로 노출 컬럼 통제 | 응답 DTO 변환 필수 — 엔티티 직접 반환 금지 |
| `<foreach> #{id}` IN 절 처리 | Spring Data JPA `In` 쿼리 메서드 또는 `@Query` + `:ids` 바인딩 |
| 동적 정렬 `<choose>` 화이트리스트 | 동적 검색은 `Specification` / `QueryDSL` 사용, 문자열 concat JPQL 금지 |
| Soft Delete `del_yn = 'N'` 조건 | `@SQLRestriction("deleted = false")` 엔티티 레벨 선언 |
| `LIMIT #{limit} OFFSET #{offset}` 페이징 | `Pageable` 파라미터 + `Page<T>` 반환 필수 |

---

## 3. 강제 사항

### 3-1. Controller (must)

- `@Valid` 를 `@RequestBody` 및 `@ModelAttribute` 에 반드시 적용한다.
- `@AuthenticationPrincipal String userId` 를 통해 인증된 사용자 범위만 접근 허용한다.
- 응답은 프로젝트 표준 `SuccessResponse<T>` 래퍼로 반환한다.
- 로그에 비밀번호 · 토큰 · 주민번호 · 카드번호를 절대 출력하지 않는다.
- 민감 필드(전화번호, 이메일)는 마스킹 후 로깅한다.

```java
// 올바른 예
@PostMapping("/phone/verify")
public ResponseEntity<SuccessResponse<Void>> verifyPhone(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody PhoneVerifyRequest request) {
    userService.verifyPhone(userId, request);
    return ResponseEntity.ok(SuccessResponse.ok());
}
```

- `PhoneVerifyRequest` 는 `com.backend.global.common.constants.ValidationConstants.PHONE_REGEX` 를 `@Pattern` 으로 참조한다.

### 3-2. Service (must)

- 도메인 진입 시 존재 여부 · 상태 · 중복을 검증하고, 실패 시 `BusinessException` 을 던진다.
- 쓰기 메서드는 `@Transactional`, 조회 메서드는 `@Transactional(readOnly = true)` 를 적용한다.
- `rollbackFor = Exception.class` 를 명시하여 checked exception 도 롤백한다.
- 감사 로그 · 외부 알림은 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 로 분리한다.
- 내부 예외는 `BusinessException` 으로 변환하여 구현 세부사항을 노출하지 않는다.

### 3-3. Repository / JPA (must)

- 모든 사용자 입력은 JPA 파라미터 바인딩(`@Param`, `:param`, `?1`)을 사용한다.
- 문자열 concat 으로 JPQL 을 조립하는 행위를 금지한다.

```java
// 금지
String jpql = "SELECT u FROM User u WHERE u.name = '" + name + "'";

// 허용
@Query("SELECT u FROM User u WHERE u.name = :name")
Optional<User> findByName(@Param("name") String name);
```

- 동적 다중 조건 검색은 `JpaSpecificationExecutor<T>` + `Specification<T>` 로 구현한다.
- 목록 조회 메서드의 반환 타입은 `Page<T>` 이며 `Pageable` 을 파라미터로 받는다.
- 엔티티를 Controller 까지 그대로 반환하는 것을 금지한다. 반드시 DTO 로 변환 후 반환한다.

### 3-4. Soft Delete (must)

- 삭제 처리는 `deleted` 플래그를 `true` 로 변경하고 `deletedAt` 에 삭제 시각을 기록한다.
- 엔티티 클래스에 `@SQLRestriction("deleted = false")` 를 선언하여 자동 필터링한다.
- 물리 DELETE 쿼리는 배치/운영 스크립트 외에서 사용하지 않는다.

### 3-5. 로깅 (must)

- SLF4J `log.warn` / `log.error` 를 사용하고, 파라미터는 `{}` 플레이스홀더로 전달한다.
- 비밀번호 · 토큰 · 주민번호 필드를 포함하는 객체를 `toString()` 으로 로깅하지 않는다.
- 전화번호 · 이메일 등 PII 는 마스킹 결과만 로그에 포함한다.

### 3-6. 응답 데이터 (should)

- 응답 DTO 는 필요한 필드만 포함하고, PII 는 권한에 따라 마스킹 여부를 분기한다.
- `@JsonIgnore` 보다 전용 응답 DTO 를 신설하는 방법을 우선한다.

---

## 4. 예시

### Specification 동적 검색

```java
public class UserSpecification {
    public static Specification<User> hasPhone(String phone) {
        return (root, query, cb) ->
            phone == null ? null : cb.equal(root.get("phone"), phone);
    }
}

// Repository
public interface UserRepository extends JpaRepository<User, String>,
        JpaSpecificationExecutor<User> { }

// Service
Page<User> result = userRepository.findAll(
    UserSpecification.hasPhone(phone), pageable);
```

### ValidationConstants 활용

```java
// com.backend.global.common.constants.ValidationConstants
@Pattern(regexp = ValidationConstants.PHONE_REGEX,
         message = ValidationConstants.PHONE_MESSAGE)
private String phone;
```

### Soft Delete 엔티티

```java
@SQLRestriction("deleted = false")
@Entity
public class User extends BaseEntity {
    private boolean deleted = false;
    private LocalDateTime deletedAt;

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
```

---

## 5. 체크리스트

### Controller
- [ ] `@RequestBody` / `@ModelAttribute` 에 `@Valid` 적용
- [ ] `@AuthenticationPrincipal String userId` 로 사용자 범위 제한
- [ ] `SuccessResponse<T>` 표준 포맷으로 응답
- [ ] 비밀번호 · 토큰 로그 출력 없음
- [ ] 전화번호 · 이메일 로그 마스킹 적용

### Service
- [ ] 도메인 검증(존재/상태/중복) 구현
- [ ] 쓰기 `@Transactional`, 조회 `@Transactional(readOnly = true)` 구분
- [ ] `rollbackFor = Exception.class` 명시
- [ ] 감사 로그는 `Propagation.REQUIRES_NEW` 별도 트랜잭션
- [ ] `BusinessException` + `ErrorCode` 로 예외 처리

### Repository / JPA
- [ ] 문자열 concat JPQL 없음
- [ ] 모든 입력값 `@Param` / `:param` 바인딩
- [ ] 동적 검색 시 `Specification` 사용
- [ ] 목록 조회에 `Pageable` + `Page<T>` 적용
- [ ] 엔티티 직접 반환 없음 — DTO 변환 확인

### Soft Delete
- [ ] `@SQLRestriction("deleted = false")` 선언 확인
- [ ] 삭제 메서드가 `deletedAt` 갱신 확인
- [ ] 물리 DELETE 쿼리 미사용 확인

### 로깅
- [ ] 비밀번호 · 토큰 · 주민번호 로그 없음
- [ ] PII 마스킹 후 로깅 확인
