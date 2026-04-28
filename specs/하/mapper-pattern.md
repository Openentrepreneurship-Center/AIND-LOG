# mapper-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

두 가지 역할을 구분한다.

1. **Repository**: `JpaRepository<T, String>` 상속. 데이터 접근 전담.
2. **Domain Mapper**: Entity ↔ DTO 변환. 방식은 자유(`@Component` 클래스, static 메서드, 생성자 변환 모두 허용).

기준 구현: `com.backend.domain.user.repository.UserRepository`, `com.backend.domain.user.dto.mapper.UserResponseMapper`

---

## 2. 원칙

- Repository는 `JpaRepository<Entity, String>` 상속
- Entity ↔ DTO 변환 로직은 컨트롤러/서비스에 두지 않고 별도로 분리

---

## 3. 강제 사항

### must

- `JpaRepository<Entity, String>` 상속
- Entity ↔ DTO 변환은 별도 클래스 또는 메서드로 분리 (방식 자유)

---

## 4. 예시 코드

### 4.1 Repository

`decapet-official/backend/src/main/java/com/backend/domain/user/repository/UserRepository.java:23-42`

```java
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByIdAndDeletedAtIsNull(String id);

    default User getByIdAndDeletedAtIsNull(String id) {
        return findByIdAndDeletedAtIsNull(id)
            .orElseThrow(UserNotFoundException::new);
    }

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
```

### 4.2 Domain Mapper (@Component 방식)

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

- [ ] `JpaRepository<Entity, String>` 상속
- [ ] 파생 쿼리 메서드명 `findByXxx` 사용
- [ ] Entity ↔ DTO 변환 로직을 서비스/컨트롤러에서 분리
- [ ] 응답 DTO는 Java `record` 사용 권장
- [ ] `com.backend.domain.{x}.repository` 패키지에 위치
