# service-pattern.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

서비스 계층은 비즈니스 로직과 트랜잭션 경계를 담당한다.
단일 구현체를 원칙으로 하며, 인터페이스 분리는 선택이다.
클래스 레벨 `@Transactional`은 생략하고, 메서드 단위로 `@Transactional` 또는 `@Transactional(readOnly = true)`를 명시한다.
커스텀 예외는 반드시 `BusinessException`을 상속하며, `ErrorCode` enum과 연동한다.
repository 조회 실패는 `getByXxx()` default 메서드에서 자동으로 예외를 던지므로 서비스 내 중복 체크가 불필요하다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상 등급 | 중 등급 |
|------|---------|---------|
| 인터페이스 분리 (`*Service` + `*ServiceImpl`) | 필수 | 선택 (단일 `*Service` 클래스 허용) |
| 클래스 레벨 `@Transactional` | 필수 | 메서드 레벨 개별 선언 권장 |
| `RuntimeException` 직접 throw | 금지 | `BusinessException` 상속 필수 |
| 검색 전용 서비스 분리 | 권장 | 선택 |

---

## 3. 강제 사항

### must
- `@Service` + `@RequiredArgsConstructor` 적용
- 조회 전용 메서드에 `@Transactional(readOnly = true)` 명시
- 쓰기(상태 변경) 메서드에 `@Transactional` 명시
- 커스텀 예외는 `BusinessException` 상속 (`RuntimeException` 직접 throw 금지)
- 도메인 간 데이터 접근은 타 도메인 서비스 호출로만 허용 (repository 직접 주입 금지)
- `userRepository.getByIdAndDeletedAtIsNull(userId)` 같이 존재 보장 메서드(`getByXxx`) 사용

### should
- 조회와 쓰기 메서드를 같은 클래스 내에서 명확히 구분
- 상태 변경 로직은 엔티티 메서드에 위임 (서비스에서 직접 필드 접근 금지)
- 복잡한 비동기 처리는 `ApplicationEventPublisher` + 이벤트 리스너로 분리
- 트랜잭션 내 외부 API 호출 최소화

---

## 4. 예시 코드

실제 `UserService.java` 패턴 인용:

```java
// decapet-official/backend/.../user/service/UserService.java:30-127
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    // ...

    // 조회 전용: readOnly = true 명시
    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        return userResponseMapper.toResponse(user);
    }

    // 쓰기: @Transactional 명시
    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        // 상태 변경은 엔티티 메서드에 위임
        user.updateProfile(userMapper.toProfileUpdateInfo(request));
        return userResponseMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        refreshTokenRepository.deleteByUserId(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.delete();
        // 비동기 cascade는 트랜잭션 커밋 후 이벤트로 처리
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }

    @Transactional
    public void changePassword(String userId, String accessToken,
                               String currentPassword, String newPassword) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException(); // BusinessException 상속
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
        tokenService.blacklistAccessToken(accessToken);
    }
}
```

커스텀 예외 패턴 (`InvalidPasswordException`은 `BusinessException` 상속):

```java
// com.backend.domain.user.exception.InvalidPasswordException
public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
```

`getByXxx()` default 메서드 패턴 (서비스에서 `orElseThrow` 중복 작성 불필요):

```java
// decapet-official/backend/.../user/repository/UserRepository.java:39-42
default User getByIdAndDeletedAtIsNull(String id) {
    return findByIdAndDeletedAtIsNull(id)
        .orElseThrow(UserNotFoundException::new);
}
```

---

## 5. 체크리스트

- [ ] `@Service` + `@RequiredArgsConstructor` 적용되었는가
- [ ] 조회 메서드에 `@Transactional(readOnly = true)` 명시되었는가
- [ ] 쓰기 메서드에 `@Transactional` 명시되었는가
- [ ] 커스텀 예외가 `BusinessException`을 상속하는가
- [ ] 도메인 간 접근이 타 서비스 호출로만 이루어지는가 (cross-repository 금지)
- [ ] 엔티티 상태 변경이 엔티티 메서드에 위임되는가 (직접 필드 조작 금지)
- [ ] repository 조회에 `getByXxx()` default 메서드를 활용하는가
- [ ] 트랜잭션 경계 밖 외부 I/O(SMS, 이벤트 발행)가 커밋 후 실행되도록 처리되었는가
- [ ] 서비스 내 불필요한 `orElseThrow` 중복 코드가 없는가
