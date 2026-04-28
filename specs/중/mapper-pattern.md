# mapper-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

두 가지 역할을 구분한다.

1. **Repository**: `JpaRepository<T, String>` 상속 인터페이스. 데이터 접근 전담.
2. **Domain Mapper**: `@Component` 클래스. Entity ↔ DTO 변환 전담.

기준 구현: `com.backend.domain.user.repository.UserRepository`, `com.backend.domain.user.dto.mapper.UserMapper`, `com.backend.domain.user.dto.mapper.UserResponseMapper`

---

## 2. 원칙 / 패턴 설명

### 2.1 Repository

- `JpaRepository<Entity, String>` 상속 (ULID String PK)
- 파생 쿼리: `findByXxx`, `existsByXxx` 네이밍
- 단건 조회 시 `default getByXxx()` 메서드로 `orElseThrow` 캡슐화

### 2.2 Domain Mapper

- `@Component` 클래스로 Spring 빈 관리
- `toEntity()`: DTO → Entity 변환
- `toResponse()`: Entity → 응답 DTO 변환
- 모든 필드를 명시적으로 매핑 (자동 매핑 프레임워크 미사용)

---

## 3. 강제 사항

### must

**Repository**
- `JpaRepository<Entity, String>` 상속
- `findByXxx` / `existsByXxx` 네이밍 준수
- 단건 조회에 대응하는 `default getByXxx()` 정의

**Domain Mapper**
- `@Component` 선언
- `toResponse()` 메서드 구현

### should

- `validateXxxNotDuplicate()` 중복 검증을 Repository default 메서드로 제공
- 복수 응답 변환용 `toListResponse()` 메서드 추가

---

## 4. 예시 코드

### 4.1 Repository — default getByXxx

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:37-53`

```java
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByIdAndDeletedAtIsNull(String id);

    default User getByIdAndDeletedAtIsNull(String id) {
        return findByIdAndDeletedAtIsNull(id)
            .orElseThrow(UserNotFoundException::new);
    }

    boolean existsByEmailAndDeletedAtIsNull(String email);

    default void validateEmailNotDuplicate(String email) {
        if (existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }
}
```

### 4.2 Domain Mapper

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserMapper.java:13-42`

```java
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .name(request.name())
                .build();
    }
}
```

`decapet-official/backend/src/main/java/com/backend/domain/user/dto/mapper/UserResponseMapper.java:9-25`

```java
@Component
public class UserResponseMapper {

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

---

## 5. 체크리스트

**Repository**
- [ ] `JpaRepository<Entity, String>` 상속
- [ ] 파생 쿼리 메서드명 `findByXxx` / `existsByXxx` 준수
- [ ] 단건 조회에 대응하는 `default getByXxx()` 정의
- [ ] `getByXxx` 내부에서 도메인 예외 throw

**Domain Mapper**
- [ ] `@Component` 선언
- [ ] `toEntity()` 구현 (DTO → Entity)
- [ ] `toResponse()` 구현 (Entity → 응답 DTO)
- [ ] 자동 매핑 프레임워크 미사용 (명시적 필드 매핑)
- [ ] 응답 DTO는 Java `record` 사용
