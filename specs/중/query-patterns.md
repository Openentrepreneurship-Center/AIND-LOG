# query-patterns.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

JPA + Spring Data JPA를 데이터 접근 기술로 사용한다. 파생 쿼리(derived query)를 기본으로 하고, 복잡한 경우 JPQL `@Query`를 사용한다. N+1 방지를 위해 `@EntityGraph`를 적용하며, 목록 조회는 `Pageable`로 페이징한다.

기준 구현: `com.backend.domain.user.repository.UserRepository`

---

## 2. 원칙 / 패턴 설명

### 2.1 쿼리 선택 기준

| 유형 | 사용 조건 |
|------|-----------|
| 파생 쿼리 (`findByXxx`) | 단순 단일/복합 컬럼 조건 |
| JPQL `@Query` | JOIN, 복잡한 조건, 파생 쿼리로 표현 어려운 경우 |
| `@EntityGraph` | 연관 엔티티 N+1 방지 |
| `Pageable` + `Page<T>` | 목록 조회 페이징 |

### 2.2 Soft Delete

엔티티에 `@SQLRestriction("deleted_at IS NULL")` 선언 시 모든 쿼리에 자동 조건 추가된다.

### 2.3 페이징

목록 조회는 반드시 `Pageable`을 파라미터로 받아 `Page<T>` 또는 `Slice<T>`를 반환한다.

---

## 3. 강제 사항

### must

- 파생 쿼리 네이밍 `findByXxx` 준수
- 연관 엔티티 조회 시 `@EntityGraph` 적용 (N+1 방지)
- 목록 조회에 `Pageable` + `Page<T>` 사용

### should

- 동적 다중 조건 검색은 `JpaSpecificationExecutor` + `Specification` 사용
- `@Modifying` 사용 시 `clearAutomatically = true` 설정

---

## 4. 예시 코드

### 4.1 파생 쿼리

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:31-49`

```java
Optional<User> findByEmail(String email);
Optional<User> findByIdAndDeletedAtIsNull(String id);
Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
boolean existsByPhoneAndDeletedAtIsNull(String phone);
boolean existsByEmailAndDeletedAtIsNull(String email);
```

### 4.2 @EntityGraph — N+1 방지

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:25-26`

```java
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);
```

### 4.3 JPQL @Query — 복잡한 연관 조회

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:28-29`

```java
@Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.vets WHERE p.user.id IN :userIds AND p.deletedAt IS NULL")
List<Pet> findPetsWithVetsByUserIds(@Param("userIds") List<String> userIds);
```

### 4.4 Soft Delete 엔티티 선언

`decapet-official/backend/src/main/java/com/backend/domain/user/entity/User.java:37`

```java
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity { }
```

### 4.5 @Modifying — 벌크 처리

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:82-84`

```java
// nativeQuery 사용 이유: @ElementCollection 테이블 직접 DELETE 필요
@Modifying
@Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
void deletePermissionsByUserId(@Param("userId") String userId);
```

---

## 5. 체크리스트

- [ ] 단순 조건은 파생 쿼리(`findByXxx`) 사용
- [ ] 복잡 조건은 JPQL `@Query` 사용
- [ ] 연관 엔티티 조회 시 `@EntityGraph` 적용
- [ ] 목록 조회에 `Pageable` + `Page<T>` 사용 (무한 조회 금지)
- [ ] soft-delete 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 선언
- [ ] `@Modifying` 사용 시 `clearAutomatically = true` 설정 (권장)
- [ ] nativeQuery 사용 시 이유 주석 작성
- [ ] 동적 검색 필요 시 `JpaSpecificationExecutor` + `Specification` 사용 (권장)
