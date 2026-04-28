# mapper-pattern.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

이 프로젝트에서 "Mapper"는 두 가지 개념을 가리킨다.

1. **Repository (데이터 접근 계층)**: `JpaRepository<T, String>`을 상속하는 Spring Data JPA 인터페이스.
   ULID 문자열 PK를 사용하므로 제네릭 두 번째 타입은 항상 `String`이다.
2. **Domain Mapper (Entity-DTO 변환)**: `@Component` 클래스로, `toEntity()` / `toResponse()` 메서드를 명시적으로 작성한다.
   MapStruct는 사용하지 않으며, 수동 변환 코드를 유지한다.

xml 기반 쿼리 파일(`Mapper.xml`, `#{...}`, `<foreach>`)은 이 프로젝트에서 사용하지 않는다.
모든 데이터 접근은 JPA + Spring Data, 필요시 JPQL `@Query` 또는 `Specification`으로 구현한다.

---

## 2. 변경/완화 사항 (상 등급 대비)

상 등급 스펙의 `@Mapper` 어노테이션, xml Mapper 파일, `#{}` 바인딩, `ResultMap`, `<foreach>` 등
xml 기반 데이터 접근 패턴은 이 프로젝트에 적용하지 않는다. 해당 섹션은 아래 JPA 패턴으로 전면 대체한다.

| 상 등급 항목 | 중 등급 대체 |
|-------------|------------|
| `@Mapper` 인터페이스 + xml | `JpaRepository<T, String>` 인터페이스 |
| `#{param}` 바인딩 | derived query 또는 `@Query` JPQL `?1` / `:param` |
| `<foreach>` IN 절 | derived query `findByIdIn(List<String>)` 또는 JPQL |
| `ResultMap` | `@EntityGraph` + JPA fetch 전략 |
| 배치 insert/update xml | `saveAll()` 또는 `@Modifying @Query` |
| `@Mapper` `@Component` 선언 | Entity↔DTO 변환 전용 `@Component *Mapper` 클래스 |

---

## 3. 강제 사항

### Repository (데이터 접근)

**must**
- `JpaRepository<Entity, String>` 상속 (ULID PK이므로 `String` 고정)
- 조회 메서드 네이밍: `findByXxx`, `existsByXxx`, `countByXxx` 규칙 준수
- 조회 실패 처리를 위한 `default getByXxx()` 메서드 선언 — 내부에서 `orElseThrow(XxxNotFoundException::new)` 호출
- 도메인 간 cross-domain repository 직접 주입 금지 (타 도메인 서비스 호출만 허용)
- soft-delete 필터는 엔티티 `@SQLRestriction("deleted_at IS NULL")` 으로 선언, repository 메서드에서 중복 조건 작성 금지

**should**
- 검증 목적 default 메서드(`validateXxxNotDuplicate`) repository 내 선언
- `JpaSpecificationExecutor<T>` 병행 상속으로 동적 검색 가능하게 유지

### Domain Mapper (Entity-DTO 변환)

**must**
- `@Component` 어노테이션 선언
- `toEntity(RequestDto)` 및 `toResponse(Entity)` 메서드 명시적 구현
- MapStruct 코드 생성 도구 사용 금지
- 변환 로직을 서비스 또는 컨트롤러에 인라인 작성 금지 (반드시 Mapper 클래스로 분리)

**should**
- 의존성이 필요한 경우(예: `PasswordEncoder`) `@RequiredArgsConstructor`로 주입
- 응답 변환 전용 mapper와 엔티티 생성 mapper를 역할에 따라 분리 (`UserMapper` vs `UserResponseMapper`)

---

## 4. 예시 코드

### Repository — `com.backend.domain.user.repository.UserRepository`

```java
// decapet-official/backend/.../user/repository/UserRepository.java:23-85
public interface UserRepository extends JpaRepository<User, String>,
        JpaSpecificationExecutor<User> {

    // derived query (soft-delete 조건 포함)
    Optional<User> findByIdAndDeletedAtIsNull(String id);

    // getByXxx() — 조회 실패 시 자동 예외 throw
    default User getByIdAndDeletedAtIsNull(String id) {
        return findByIdAndDeletedAtIsNull(id)
            .orElseThrow(UserNotFoundException::new);
    }

    boolean existsByPhoneAndDeletedAtIsNull(String phone);
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // 검증 로직을 repository default 메서드로 캡슐화
    default void validateEmailNotDuplicate(String email) {
        if (existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }

    // @EntityGraph 로 N+1 방지
    @EntityGraph(attributePaths = {"pets", "permissions"})
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    // native 쿼리 사용 시 이유 주석 필수
    @Modifying
    @Query(value = "DELETE FROM user_permissions WHERE user_id = :userId",
           nativeQuery = true) // JPA cascade가 @ElementCollection 조인테이블에 직접 도달하지 못해 native 사용
    void deletePermissionsByUserId(@Param("userId") String userId);
}
```

### Domain Mapper — `UserMapper.java`

```java
// decapet-official/backend/.../user/dto/mapper/UserMapper.java:13-42
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    // Entity 생성
    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .name(request.name())
                .build();
    }

    // 내부 전달 객체 변환
    public ProfileUpdateInfo toProfileUpdateInfo(UpdateProfileRequest request) {
        return new ProfileUpdateInfo(
                request.name(), null, request.zipCode(),
                request.address(), request.detailAddress(),
                request.recipientName(), request.recipientPhone()
        );
    }
}
```

```java
// decapet-official/backend/.../user/dto/mapper/UserResponseMapper.java:8-25
@Component
public class UserResponseMapper {

    // DTO 응답 변환
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getPhone(),
                user.getName(), user.getZipCode(), user.getAddress(),
                user.getDetailAddress(), user.getRecipientName(),
                user.getRecipientPhone(), user.getPermissions()
        );
    }
}
```

---

## 5. 체크리스트

- [ ] Repository가 `JpaRepository<Entity, String>`을 상속하는가 (ULID PK)
- [ ] 조회 실패 처리를 위한 `default getByXxx()` 메서드가 선언되었는가
- [ ] `JpaSpecificationExecutor<T>`가 병행 상속되어 있는가
- [ ] cross-domain repository 직접 주입이 없는가
- [ ] Entity↔DTO 변환이 `@Component *Mapper` 클래스로 분리되었는가
- [ ] `toEntity()` / `toResponse()` 메서드가 명시적으로 구현되었는가
- [ ] MapStruct 의존성 및 어노테이션이 없는가
- [ ] 검증 로직이 `validateXxxNotDuplicate()` default 메서드로 캡슐화되었는가
- [ ] native 쿼리 사용 시 이유 주석이 작성되었는가
- [ ] soft-delete 조건이 `@SQLRestriction`으로 엔티티에 선언되었는가
