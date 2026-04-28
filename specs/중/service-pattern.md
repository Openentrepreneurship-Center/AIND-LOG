# service-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

서비스 계층은 비즈니스 로직 진입점이다. 트랜잭션 관리, 엔티티 조회, 도메인 메서드 호출, 커스텀 예외 발생을 담당한다. 컨트롤러로부터 식별자와 DTO를 수신하고, Repository와 Domain Mapper를 통해 처리 결과를 반환한다.

기준 구현: `com.backend.domain.user.service.UserService`

---

## 2. 원칙 / 패턴 설명

### 2.1 클래스 구조

- `@Service` + `@RequiredArgsConstructor` 클래스 단일 구현
- 인터페이스 분리는 선택 사항이나 테스트 용이성을 위해 권장

### 2.2 트랜잭션 전략

- 클래스 레벨 `@Transactional` 선언으로 기본 쓰기 트랜잭션 적용
- 조회 전용 메서드에는 `@Transactional(readOnly = true)` 명시 권장
- 쓰기 메서드는 클래스 레벨 상속으로 트랜잭션 보장

### 2.3 엔티티 조회

Repository의 `default getByXxx()` 메서드를 사용한다. `Optional` 직접 처리 코드를 서비스 메서드 내부에 작성하지 않는다.

### 2.4 예외 처리

비즈니스 위반은 `BusinessException` 하위 커스텀 예외를 throw한다. `RuntimeException` 직접 throw는 금지한다.

---

## 3. 강제 사항

### must

- `@Service` 선언
- `@RequiredArgsConstructor` 선언 (생성자 주입)
- 클래스 레벨 `@Transactional` 선언
- `getByXxx()` default 메서드로 엔티티 조회
- 커스텀 예외(`BusinessException` 하위) throw

### should

- 조회 메서드에 `@Transactional(readOnly = true)` 명시
- 쓰기 메서드는 클래스 레벨 트랜잭션 상속
- 외부 서비스 호출은 트랜잭션 커밋 후 실행 (`TransactionTemplate` 또는 이벤트)
- 인터페이스 분리 (`{Domain}Service` + 구현체)

---

## 4. 예시 코드

### 4.1 서비스 기본 구조

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:30-64`

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        refreshTokenRepository.deleteByUserId(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.delete();
        eventPublisher.publishEvent(new UserDeletedEvent(userId)); // 커밋 후 cascade 처리
    }
}
```

### 4.2 커스텀 예외

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:109-114`

```java
@Transactional
public void changePassword(String userId, String accessToken, String currentPassword, String newPassword) {
    User user = userRepository.getByIdAndDeletedAtIsNull(userId);

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new InvalidPasswordException(); // BusinessException 하위 커스텀 예외
    }

    user.updatePassword(passwordEncoder.encode(newPassword));
}
```

---

## 5. 체크리스트

- [ ] `@Service` 선언
- [ ] `@RequiredArgsConstructor` 선언
- [ ] 클래스 레벨 `@Transactional` 선언
- [ ] 조회 메서드에 `@Transactional(readOnly = true)` (권장)
- [ ] 엔티티 조회 시 `getByXxx()` default 메서드 사용
- [ ] 모든 예외는 `BusinessException` 하위 커스텀 예외
- [ ] 예외 클래스는 `com.backend.domain.{x}.exception` 패키지
- [ ] 컨트롤러 응답 처리 코드(`ResponseEntity` 등) 없음
- [ ] 엔티티 상태 변경은 도메인 메서드로만 처리
- [ ] 외부 서비스 호출은 트랜잭션 외부 실행 처리 (권장)
