# service-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

서비스 계층은 비즈니스 로직을 처리한다. 트랜잭션 관리, 엔티티 조회, 도메인 메서드 호출, 예외 발생을 담당한다.

기준 구현: `com.backend.domain.user.service.UserService`

---

## 2. 원칙

- `@Service` + `@RequiredArgsConstructor` 선언
- 클래스 레벨 `@Transactional` 1개
- 예외는 커스텀 예외로 throw

---

## 3. 강제 사항

### must

- `@Service` 선언
- `@RequiredArgsConstructor` (생성자 주입)
- 클래스 레벨 `@Transactional` 선언
- 예외 발생 시 `BusinessException` 하위 커스텀 예외 사용

---

## 4. 예시 코드

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:30-54`

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        return userResponseMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        user.updateProfile(userMapper.toProfileUpdateInfo(request));
        return userResponseMapper.toResponse(user);
    }
}
```

커스텀 예외 정의 패턴 (`com.backend.domain.user.exception`):

```java
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

---

## 5. 체크리스트

- [ ] `@Service` 선언
- [ ] `@RequiredArgsConstructor` 선언
- [ ] 클래스 레벨 `@Transactional` 선언
- [ ] 엔티티 조회 시 `getByXxx()` 메서드 사용 권장
- [ ] 예외는 `BusinessException` 하위 커스텀 예외로 throw
- [ ] 컨트롤러 응답 처리 코드 없음
- [ ] `com.backend.domain.{x}.service` 패키지에 위치
