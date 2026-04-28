# query-patterns.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

본 프로젝트는 JPA + Spring Data JPA만을 데이터 접근 기술로 사용한다. 쿼리 작성은 파생 쿼리(derived query), JPQL `@Query`, `@EntityGraph`, `JpaSpecificationExecutor`, `@Modifying` 순으로 단순한 것을 우선 적용한다. 네이티브 쿼리는 JPA로 표현 불가능한 경우에만 허용하며, 반드시 사용 이유를 주석으로 기재한다.

기준 구현: `com.backend.domain.user.repository.UserRepository`

---

## 2. 원칙 / 패턴 설명

### 2.1 쿼리 유형 선택 기준

| 유형 | 사용 조건 |
|------|-----------|
| 파생 쿼리 (`findByXxx`) | 단순 단일/복합 컬럼 조건 |
| JPQL `@Query` | 파생 쿼리로 표현이 복잡한 경우, JOIN, GROUP BY 필요 시 |
| `@EntityGraph` | 연관 엔티티 N+1 방지 fetch join |
| `JpaSpecificationExecutor` | 동적 다중 조건 검색 |
| `@Modifying` + `@Query` | 부분 업데이트, 벌크 삭제 |
| nativeQuery = true | JPA JPQL로 표현 불가한 DB 전용 기능 사용 시 (이유 주석 필수) |

### 2.2 Soft Delete 처리

엔티티에 `@SQLRestriction("deleted_at IS NULL")`을 선언하면 Hibernate가 모든 쿼리에 자동으로 조건을 추가한다. 이 어노테이션이 있는 엔티티는 조회 시 별도 `deletedAt` 조건을 쿼리에 추가하지 않아도 된다.

### 2.3 사용자 경계 격리

다른 사용자의 데이터에 접근하는 것을 Repository 레벨에서 차단한다. `findByIdAndOwnerId` 패턴으로 소유권 검증을 쿼리에 포함시킨다.

### 2.4 페이징

무한 결과 조회는 허용하지 않는다. 목록 조회는 반드시 `Pageable`을 파라미터로 받고 `Page<T>` 또는 `Slice<T>`를 반환한다.

---

## 3. 강제 사항

### must (반드시 준수)

| 항목 | 내용 |
|------|------|
| 파생 쿼리 네이밍 | `findByEmailAndDeletedAtIsNull` 형태 준수 |
| `@EntityGraph` 또는 fetch join | 연관 엔티티 조회 시 N+1 방지 필수 |
| `Pageable` + `Page<T>` / `Slice<T>` | 목록 조회 시 페이징 필수, 무한 조회 금지 |
| `JpaSpecificationExecutor` + `Specification` | 동적 다중 조건 검색 시 사용 |
| `@Modifying(clearAutomatically = true)` + `@Query` | 부분 업데이트/벌크 처리 시 필수 |
| `@SQLRestriction("deleted_at IS NULL")` | soft-delete 엔티티에 클래스 레벨 선언 |
| 사용자 경계 격리 | `findByIdAndOwnerId` 패턴으로 Repository 레벨에서 소유권 검증 |
| nativeQuery 이유 주석 | `nativeQuery = true` 사용 시 이유 주석 필수 |

### should (권장)

- JPQL 쿼리는 엔티티/필드명 기준으로 작성 (테이블/컬럼명 직접 사용 지양)
- 복잡한 `@Query`는 메서드 바로 위에 인라인 설명 주석 추가
- `@EntityGraph`의 `attributePaths`는 실제 필요한 연관만 포함

---

## 4. 예시 코드

### 4.1 파생 쿼리 — 기본 조건부 조회

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:31-48`

```java
// 단순 파생 쿼리 — soft-delete 컬럼 조건 포함
Optional<User> findByEmail(String email);
Optional<User> findByIdAndDeletedAtIsNull(String id);
Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
Optional<User> findByEmailAndPhoneAndDeletedAtIsNull(String email, String phone);

// 존재 확인
boolean existsByPhoneAndDeletedAtIsNull(String phone);
boolean existsByEmailAndDeletedAtIsNull(String email);
```

### 4.2 @EntityGraph — N+1 방지 fetch

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:25-26`

```java
// pets, permissions 연관 엔티티를 LEFT JOIN으로 함께 조회 (N+1 방지)
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);
```

### 4.3 JPQL @Query — 복잡한 연관 조회

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:28-29`

```java
// 여러 userId에 속한 Pet을 vets와 함께 DISTINCT 조회
// 파생 쿼리로 표현하기 복잡하여 JPQL 사용
@Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.vets WHERE p.user.id IN :userIds AND p.deletedAt IS NULL")
List<Pet> findPetsWithVetsByUserIds(@Param("userIds") List<String> userIds);
```

### 4.4 @Modifying — 벌크 삭제 (nativeQuery)

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:82-84`

```java
// nativeQuery 사용 이유: user_permissions는 @ElementCollection 컬렉션 테이블로
// JPQL DELETE 단독으로 처리할 수 없어 네이티브 쿼리 사용
@Modifying
@Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
void deletePermissionsByUserId(@Param("userId") String userId);
```

### 4.5 Soft Delete 엔티티 선언

`decapet-official/backend/src/main/java/com/backend/domain/user/entity/User.java:37`

```java
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")  // 모든 쿼리에 자동 조건 추가
public class User extends BaseEntity {
    // ...
}
```

### 4.6 페이징 처리 패턴

```java
// 컨트롤러 수신
@GetMapping
public ResponseEntity<SuccessResponse> listUsers(
        @AuthenticationPrincipal String userId,
        Pageable pageable) {
    Page<UserResponse> page = userService.listUsers(pageable);
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_LIST_SUCCESS, page));
}

// Repository
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);
```

### 4.7 동적 검색 — Specification

```java
// Repository 선언
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> { }

// Specification 조합 예시
public class UserSpecification {
    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
            email == null ? null : cb.equal(root.get("email"), email);
    }

    public static Specification<User> hasName(String name) {
        return (root, query, cb) ->
            name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }
}

// 서비스에서 조합 사용
Specification<User> spec = Specification
    .where(UserSpecification.hasEmail(request.email()))
    .and(UserSpecification.hasName(request.name()));
Page<User> result = userRepository.findAll(spec, pageable);
```

### 4.8 사용자 경계 격리 패턴

```java
// 본인 소유 데이터만 접근 — Repository 레벨에서 소유권 강제
Optional<Pet> findByIdAndUserId(String petId, String userId);

default Pet getByIdAndUserId(String petId, String userId) {
    return findByIdAndUserId(petId, userId)
        .orElseThrow(PetNotFoundException::new);
}
```

---

## 5. 체크리스트

### 쿼리 유형 선택
- [ ] 단순 조건은 파생 쿼리(`findByXxx`) 사용
- [ ] 복잡 조건은 JPQL `@Query` 사용 (테이블명 아닌 엔티티명 기준)
- [ ] nativeQuery 사용 시 `nativeQuery = true` + 이유 주석 필수

### N+1 방지
- [ ] 연관 엔티티 조회 시 `@EntityGraph(attributePaths = {...})` 또는 fetch join 적용
- [ ] `FetchType.LAZY` 연관을 N번 개별 조회하는 코드 없음

### 페이징
- [ ] 목록 조회 메서드 파라미터에 `Pageable` 포함
- [ ] 반환 타입 `Page<T>` 또는 `Slice<T>`
- [ ] `List<T>` 무한 조회 메서드 없음 (단건/소량 고정 조회 제외)

### Soft Delete
- [ ] soft-delete 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 선언
- [ ] 논리 삭제 메서드는 `entity.delete()` 도메인 메서드 호출 (UPDATE, DELETE 쿼리 직접 실행 지양)

### 부분 업데이트 / 벌크
- [ ] `@Modifying` 사용 시 `clearAutomatically = true` 설정
- [ ] 벌크 업데이트 후 영속성 컨텍스트 정합성 확인

### 동적 검색
- [ ] 다중 조건 동적 검색은 `JpaSpecificationExecutor` + `Specification` 사용
- [ ] Specification 클래스는 `{Domain}Specification`으로 분리

### 사용자 경계
- [ ] 타인 데이터 접근을 Repository 레벨에서 차단 (`findByIdAndOwnerId` 패턴)
- [ ] 서비스 레벨에서 추가 소유권 검증 이중 적용
