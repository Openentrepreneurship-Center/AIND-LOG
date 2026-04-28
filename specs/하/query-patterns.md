# query-patterns.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

JPA + Spring Data JPA를 사용한다. 파생 쿼리(`findByXxx`)를 기본으로 하고, 복잡한 경우 JPQL `@Query`를 사용한다. 페이징과 N+1 방지는 권장 사항이다.

기준 구현: `com.backend.domain.user.repository.UserRepository`

---

## 2. 원칙

- 파생 쿼리(`findByXxx`)를 기본으로 사용
- 목록 조회는 페이징 적용 권장
- 연관 엔티티 조회 시 N+1 주의

---

## 3. 강제 사항

### must

- 파생 쿼리 메서드명 `findByXxx` 준수

### 권장

- 목록 조회에 `Pageable` + `Page<T>` 사용
- 연관 엔티티 조회 시 `@EntityGraph` 적용

---

## 4. 예시 코드

### 4.1 파생 쿼리

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:31-49`

```java
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(String id);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByPhoneAndDeletedAtIsNull(String phone);
}
```

### 4.2 페이징 (권장)

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:25-26`

```java
// N+1 방지 + 페이징
@EntityGraph(attributePaths = {"pets", "permissions"})
Page<User> findAllByDeletedAtIsNull(Pageable pageable);
```

### 4.3 Soft Delete 엔티티

`decapet-official/backend/src/main/java/com/backend/domain/user/entity/User.java:37`

```java
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity { }
```

---

## 5. 체크리스트

- [ ] 파생 쿼리 메서드명 `findByXxx` 준수
- [ ] soft-delete 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 선언
- [ ] 목록 조회에 `Pageable` 적용 (권장)
- [ ] 연관 엔티티 조회 시 `@EntityGraph` 적용 (권장)
- [ ] `com.backend.domain.{x}.repository` 패키지에 위치
