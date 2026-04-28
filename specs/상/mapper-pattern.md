# mapper-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

본 프로젝트에서 "Mapper"는 두 가지 역할로 명확히 구분된다.

1. **Repository (데이터 접근 계층)**: `JpaRepository<T, String>`을 상속하는 인터페이스. 데이터베이스 접근을 담당하며, 파생 쿼리(derived query), JPQL `@Query`, `@EntityGraph`, `JpaSpecificationExecutor`를 사용한다.
2. **Domain Mapper (Entity ↔ DTO 변환)**: `@Component` 클래스. 엔티티와 DTO 사이의 명시적 변환 메서드(`toEntity`, `toResponse`, `toListResponse`)를 제공한다. MapStruct를 사용하지 않는다.

두 역할을 하나의 클래스에 혼합하지 않는다.

---

## 2. 원칙 / 패턴 설명

### 2.1 Repository 설계 원칙

- `JpaRepository<Entity, String>` 상속 (PK 타입: ULID String)
- 필요 시 `JpaSpecificationExecutor<Entity>` 추가 상속
- 파생 쿼리 메서드는 `findByXxx`, `existsByXxx` 네이밍 준수
- `Optional<T>`를 반환하는 `findByXxx` + `orElseThrow` 패턴 대신, Repository에 `default getByXxx()` 메서드를 정의하여 서비스 코드 간결화
- 중복/존재 검증 로직(`validateXxxNotDuplicate`)은 Repository `default` 메서드로 위임
- cross-domain Repository 직접 접근 금지: 다른 도메인의 데이터가 필요하면 해당 도메인 서비스를 통해 접근

### 2.2 Domain Mapper 설계 원칙

- `@Component` 클래스로 선언 (Spring 빈으로 관리)
- `@RequiredArgsConstructor`로 의존성 주입 (예: `PasswordEncoder`)
- 변환 메서드 명명: `toEntity()`, `toResponse()`, `toListResponse()`
- 모든 필드를 명시적으로 매핑. 프레임워크 자동 매핑 미사용
- 양방향 변환(Entity → DTO, DTO → Entity) 모두 한 클래스에서 관리

---

## 3. 강제 사항

### must (반드시 준수)

**Repository**

| 항목 | 내용 |
|------|------|
| `JpaRepository<T, String>` 상속 | PK가 ULID String임을 명시 |
| `findByXxx` / `existsByXxx` 네이밍 | 파생 쿼리 메서드 네이밍 규칙 준수 |
| `default getByXxx()` 메서드 | `Optional` 조회 + `orElseThrow` 로직을 Repository default 메서드로 캡슐화 |
| `validateXxxNotDuplicate()` | 중복 검증 로직을 Repository default 메서드로 제공 |
| cross-domain Repository 직접 접근 금지 | 타 도메인 데이터는 해당 도메인 서비스를 통해 접근 |

**Domain Mapper**

| 항목 | 내용 |
|------|------|
| `@Component` 선언 | Spring 빈으로 관리 |
| `toEntity()` 메서드 | DTO → Entity 변환 메서드 명시적 구현 |
| `toResponse()` 메서드 | Entity → 응답 DTO 변환 메서드 명시적 구현 |
| MapStruct 미사용 | 자동 매핑 프레임워크 사용 금지, 명시적 변환만 허용 |
| 명시적 필드 매핑 | 모든 필드를 코드에서 직접 매핑 |

### should (권장)

- `toListResponse(List<Entity>)` 메서드로 컬렉션 변환 제공
- Mapper 클래스 이름: `{Domain}Mapper` (Entity→DTO), `{Domain}ResponseMapper` (Entity→응답DTO)

---

## 4. 예시 코드

### 4.1 Repository — JpaRepository + default 메서드

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:23-85`

```java
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    // 파생 쿼리 - 단순 조회
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(String id);
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
    Optional<User> findByEmailAndPhoneAndDeletedAtIsNull(String email, String phone);

    // 존재 확인
    boolean existsByPhoneAndDeletedAtIsNull(String phone);
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // default getByXxx — 없으면 도메인 예외 자동 throw
    default User getByEmail(String email) {
        return findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    default User getByIdAndDeletedAtIsNull(String id) {
        return findByIdAndDeletedAtIsNull(id)
            .orElseThrow(UserNotFoundException::new);
    }

    default User getByPhone(String phone) {
        return findByPhoneAndDeletedAtIsNull(phone)
            .orElseThrow(UserNotFoundException::new);
    }

    default User getByEmailAndPhone(String email, String phone) {
        return findByEmailAndPhoneAndDeletedAtIsNull(email, phone)
            .orElseThrow(InvalidAccountOrPhoneException::new);
    }

    // default validateXxx — 중복 검증 로직 캡슐화
    default void validateEmailNotDuplicate(String email) {
        if (existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }

    default void validatePhoneNotDuplicate(String phone) {
        if (existsByPhoneAndDeletedAtIsNull(phone)) {
            throw new DuplicatePhoneException();
        }
    }

    // 현재 값과 다를 때만 검증 (변경 시나리오)
    default void validatePhoneNotDuplicate(String phone, String currentPhone) {
        if (phone != null && !phone.equals(currentPhone)) {
            if (existsByPhoneAndDeletedAtIsNull(phone)) {
                throw new DuplicatePhoneException();
            }
        }
    }

    // @Modifying + nativeQuery (권한 테이블 직접 삭제)
    @Modifying
    @Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
    void deletePermissionsByUserId(@Param("userId") String userId);
}
```

### 4.2 Domain Mapper — toEntity + toProfileUpdateInfo

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserMapper.java:13-42`

```java
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    // DTO → Entity 변환
    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .name(request.name())
                .zipCode(request.zipCode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .build();
    }

    // DTO → 내부 전달 객체 변환
    public ProfileUpdateInfo toProfileUpdateInfo(UpdateProfileRequest request) {
        return new ProfileUpdateInfo(
                request.name(),
                null,           // phone은 SMS 인증 플로우에서만 변경
                request.zipCode(),
                request.address(),
                request.detailAddress(),
                request.recipientName(),
                request.recipientPhone()
        );
    }
}
```

### 4.3 응답 Mapper — toResponse

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserResponseMapper.java:9-25`

```java
@Component
public class UserResponseMapper {

    // Entity → 응답 DTO 변환 (명시적 필드 매핑)
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getName(),
                user.getZipCode(),
                user.getAddress(),
                user.getDetailAddress(),
                user.getRecipientName(),
                user.getRecipientPhone(),
                user.getPermissions()
        );
    }
}
```

### 4.4 응답 DTO — record

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/response/UserResponse.java:1-19`

```java
// 응답 DTO는 Java record 사용
public record UserResponse(
    String id,
    String email,
    String phone,
    String name,
    String zipCode,
    String address,
    String detailAddress,
    String recipientName,
    String recipientPhone,
    Set<PermissionType> permissions
) {}
```

---

## 5. 체크리스트

### Repository
- [ ] `JpaRepository<Entity, String>` 상속 선언
- [ ] 파생 쿼리 메서드명 `findByXxx` / `existsByXxx` 준수
- [ ] 조회 메서드 반환 타입 `Optional<T>`
- [ ] 모든 단건 조회에 대응하는 `default getByXxx()` 메서드 정의
- [ ] `getByXxx` 내부에서 해당 도메인 예외(`NotFoundException`) throw
- [ ] 중복 검증은 `default validateXxxNotDuplicate()` 메서드로 캡슐화
- [ ] cross-domain Repository 직접 참조 없음 (타 도메인은 서비스 경유)
- [ ] 동적 검색 필요 시 `JpaSpecificationExecutor<T>` 추가 상속

### Domain Mapper
- [ ] `@Component` 선언
- [ ] `@RequiredArgsConstructor` 선언 (의존성 있는 경우)
- [ ] `toEntity()` 메서드 구현 (DTO → Entity)
- [ ] `toResponse()` 메서드 구현 (Entity → 응답 DTO)
- [ ] MapStruct 등 자동 매핑 프레임워크 미사용
- [ ] 모든 필드 명시적 매핑 (누락 필드 없음)
- [ ] 컬렉션 변환 필요 시 `toListResponse(List<Entity>)` 메서드 추가

### 공통
- [ ] 응답 DTO는 Java `record` 사용
- [ ] Mapper 클래스 패키지: `com.backend.domain.{x}.dto.mapper`
- [ ] Repository 패키지: `com.backend.domain.{x}.repository`
