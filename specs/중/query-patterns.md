# query-patterns.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

이 프로젝트는 JPA + Spring Data 기반으로만 데이터를 조회·변경한다.
xml 기반 쿼리 파일과 파라미터 바인딩 문법(`#{}`, `${}`)은 사용하지 않는다.
기본 조회는 derived query, 복잡한 조건은 JPQL `@Query`, 동적 검색은 `Specification`으로 작성한다.
N+1 문제 방지와 페이징 처리는 중 등급에서 필수 사항이다.

---

## 2. 변경/완화 사항 (상 등급 대비)

상 등급 스펙의 xml 기반 동적 쿼리(`<where>`, `<if>`, `<foreach>`, `ResultMap`, `<choose>`, CTE `<include>` 등)는
이 프로젝트에 존재하지 않는다. 아래 JPA 패턴이 전면 대체한다.

| 상 등급 패턴 | 중 등급 JPA 대체 |
|------------|----------------|
| `<where>` + `<if>` 동적 쿼리 | `Specification` + `JpaSpecificationExecutor` |
| `<foreach>` IN 절 | derived query `findByIdIn(List<String>)` |
| `ResultMap` 연관 매핑 | `@EntityGraph(attributePaths={...})` 또는 fetch join |
| `LIMIT #{limit} OFFSET #{offset}` | `Pageable` + `Page<T>` / `Slice<T>` |
| 배치 insert xml | `repository.saveAll(List<Entity>)` |
| 배치 update xml | `@Modifying @Query` |
| `AND status != 'DELETED'` 조건 | 엔티티 `@SQLRestriction("deleted_at IS NULL")` |

---

## 3. 강제 사항

### must
- 단순 조회: derived query 네이밍 규칙 (`findByEmailAndDeletedAtIsNull`, `existsByPhone`)
- 복잡한 조회: `@Query` JPQL 사용. native 쿼리는 `nativeQuery = true` + 이유 주석 필수
- N+1 방지: 연관 엔티티 함께 조회 시 `@EntityGraph` 또는 fetch join 적용
- 페이징: 목록 조회는 `Pageable` 파라미터 + `Page<T>` 또는 `Slice<T>` 반환, 무한 조회(전건 조회) 금지
- 부분 업데이트 JPQL: `@Modifying` + `@Query` 조합, `clearAutomatically = true` 설정
- 사용자 경계 보안: 단건 조회는 `findByIdAndUserId(id, userId)` 형태로 소유권 조건 포함
- soft-delete: 엔티티 레벨 `@SQLRestriction("deleted_at IS NULL")` 선언으로 자동 필터, 쿼리에서 중복 조건 작성 금지

### should
- 동적 검색 조건: `JpaSpecificationExecutor<T>` + `Specification<T>` 조합 사용
- 카운트 쿼리와 데이터 쿼리 분리가 필요한 경우 `@Query(countQuery = "...")` 명시
- 반환 타입이 단건인 경우 `Optional<T>` 사용, nullable 반환 금지

---

## 4. 예시 코드

### derived query — 기본 조회 (`com.backend.domain.user.repository.UserRepository`)

```java
// decapet-official/backend/.../user/repository/UserRepository.java:32-53
Optional<User> findByEmail(String email);
Optional<User> findByIdAndDeletedAtIsNull(String id);
boolean existsByPhoneAndDeletedAtIsNull(String phone);
boolean existsByEmailAndDeletedAtIsNull(String email);
Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
Optional<User> findByEmailAndPhoneAndDeletedAtIsNull(String email, String phone);
```

### @EntityGraph — N+1 방지

```java
// decapet-official/backend/.../user/repository/UserRepository.java:25-26
// pets, permissions 연관 엔티티를 한 쿼리로 즉시 로딩
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);
```

### @Query JPQL — 복잡한 fetch join

```java
// decapet-official/backend/.../user/repository/UserRepository.java:28-29
// Pet→Vet 2depth 연관을 fetch join으로 한 번에 로딩 (N+1 방지)
@Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.vets WHERE p.user.id IN :userIds AND p.deletedAt IS NULL")
List<Pet> findPetsWithVetsByUserIds(@Param("userIds") List<String> userIds);
```

### @Modifying — 부분 업데이트 / 삭제

```java
// decapet-official/backend/.../user/repository/UserRepository.java:82-84
// JPA cascade가 @ElementCollection 조인 테이블에 직접 도달하지 못하므로 native 사용
@Modifying
@Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
void deletePermissionsByUserId(@Param("userId") String userId);
```

JPQL `@Modifying` 예시 (native 아닌 경우):

```java
// 일반 JPQL 부분 업데이트 — 영속성 컨텍스트 초기화 필요
@Modifying(clearAutomatically = true)
@Query("UPDATE Pet p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.user.id = :userId")
void softDeleteByUserId(@Param("userId") String userId);
```

### Pageable — 페이징 처리

```java
// 페이지 단위 목록 조회 — Pageable 파라미터 필수
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);

// 서비스에서 Pageable 생성
PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
Page<User> result = userRepository.findAllByDeletedAtIsNull(pageRequest);
```

### Specification — 동적 검색

```java
// JpaSpecificationExecutor 상속 선언
public interface UserRepository extends JpaRepository<User, String>,
        JpaSpecificationExecutor<User> { ... }

// Specification 사용 예시
Specification<User> spec = Specification
    .where(UserSpecs.hasName(name))
    .and(UserSpecs.hasEmail(email));
Page<User> result = userRepository.findAll(spec, pageable);
```

### soft-delete — @SQLRestriction

```java
// decapet-official/backend/.../user/entity/User.java:37
// 엔티티 레벨 선언으로 모든 쿼리에 자동 필터 적용
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity { ... }
```

---

## 5. 체크리스트

- [ ] 단순 조회에 derived query 네이밍 규칙이 적용되었는가
- [ ] 복잡한 조회에 JPQL `@Query`를 사용하는가
- [ ] native 쿼리 사용 시 `nativeQuery = true` + 이유 주석이 있는가
- [ ] 연관 엔티티 조회 시 `@EntityGraph` 또는 fetch join으로 N+1을 방지하는가
- [ ] 목록 조회에 `Pageable` + `Page<T>` 가 적용되었는가 (전건 조회 없음)
- [ ] `@Modifying @Query`에 `clearAutomatically = true`가 설정되었는가
- [ ] 단건 소유권 조회에 사용자 ID 조건이 포함되었는가 (`findByIdAndUserId`)
- [ ] soft-delete 조건이 `@SQLRestriction`으로 엔티티에 선언되었는가
- [ ] 동적 검색에 `Specification`을 사용하는가
- [ ] 반환 단건이 `Optional<T>` 타입인가 (nullable 반환 금지)
