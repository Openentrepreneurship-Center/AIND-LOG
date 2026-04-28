# service-pattern.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

## 1. 개요

서비스 계층은 도메인 비즈니스 로직의 진입점이다. 트랜잭션 경계 관리, 도메인 객체 협력 조정, 커스텀 예외 발생, 이벤트 발행을 담당한다. 컨트롤러로부터 식별자(userId 등)와 DTO를 수신하고, Repository를 통해 엔티티를 가져와 도메인 메서드를 호출하며, 결과를 DTO로 변환해 반환한다.

본 프로젝트 서비스 구현의 기준은 `com.backend.domain.user.service.UserService`이다.

---

## 2. 원칙 / 패턴 설명

### 2.1 인터페이스 분리

서비스는 **인터페이스(`{Domain}Service`) + 구현체(`{Domain}ServiceImpl`)** 형태로 분리한다. 단, 구현체가 1개로 확정된 경우에도 인터페이스 분리를 적용하여 테스트 가능성과 교체 유연성을 확보한다.

현재 `UserService`는 단일 클래스로 구현되어 있으나, 신규 도메인은 인터페이스를 분리해 작성한다.

### 2.2 트랜잭션 전략

- 클래스 레벨 `@Transactional` 선언으로 기본 쓰기 트랜잭션 보장
- 조회 전용 메서드는 반드시 `@Transactional(readOnly = true)` 명시
- 쓰기 메서드는 클래스 레벨에서 상속하되, 특수 전파 정책이 필요한 경우 메서드 레벨에 재선언

### 2.3 도메인 검증 메서드 분리

중복 확인, 존재 확인 등의 검증 로직은 Repository의 `default` 메서드(`validateXxxNotDuplicate`)로 위임하거나 서비스 내 private 메서드로 분리한다. 인라인 if 분기로 작성하지 않는다.

### 2.4 엔티티 조회 패턴

엔티티 조회 시 `Optional` 직접 처리 대신 Repository에 정의된 `getByXxx()` default 메서드를 사용한다. 이 메서드는 없을 경우 즉시 도메인 예외를 던진다.

```java
// 금지
userRepository.findByIdAndDeletedAtIsNull(userId)
    .orElseThrow(() -> new UserNotFoundException());

// 권장
User user = userRepository.getByIdAndDeletedAtIsNull(userId);
```

### 2.5 도메인 이벤트

트랜잭션 커밋 이후 비동기로 처리해야 하는 cascade 작업(연관 데이터 삭제, 외부 서비스 호출 등)은 `ApplicationEventPublisher`로 도메인 이벤트를 발행한다. 이벤트 클래스는 `record`로 정의한다.

---

## 3. 강제 사항

### must (반드시 준수)

| 항목 | 내용 |
|------|------|
| `@Service` | 구현체 클래스에 선언 |
| `@RequiredArgsConstructor` | 생성자 주입 전용. `@Autowired` 필드 주입 금지 |
| 클래스 레벨 `@Transactional` | 쓰기 기본값 보장 |
| 조회 메서드 `@Transactional(readOnly = true)` | 모든 조회 전용 메서드에 명시 |
| 인터페이스 분리 | `{Domain}Service` 인터페이스 + `{Domain}ServiceImpl` 구현체 |
| `getByXxx()` default 메서드 사용 | Repository에서 정의한 예외 자동 throw 메서드 활용 |
| 도메인 검증 메서드 분리 | 중복/존재 검증은 Repository `validateXxx` 또는 private 메서드로 분리 |
| 도메인 이벤트 (`UserDeletedEvent` 패턴) | 트랜잭션 후 처리 작업은 `ApplicationEventPublisher` 사용 |
| 커스텀 예외 throw | `BusinessException` 하위 커스텀 예외만 사용. `RuntimeException` 직접 throw 금지 |

### should (권장)

- 메서드 하나의 책임은 단일 비즈니스 시나리오 처리
- 외부 서비스 호출(`SmsService` 등)은 트랜잭션 외부에서 실행하거나 `TransactionTemplate`으로 경계를 명시적으로 제어
- 복잡한 생성 로직은 팩토리 메서드나 Mapper로 위임

---

## 4. 예시 코드

### 4.1 기본 서비스 구조

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:30-55`

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserResponseMapper userResponseMapper;
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
}
```

### 4.2 도메인 이벤트 발행 패턴

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:67-77`

```java
@Transactional
public void deleteUser(String userId) {
    User user = userRepository.getByIdAndDeletedAtIsNull(userId);

    // 즉시 처리: 토큰 무효화 + PII 익명화 + soft delete
    refreshTokenRepository.deleteByUserId(userId);
    user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()));
    user.delete();

    // 비동기 cascade 삭제 (트랜잭션 커밋 후 실행)
    eventPublisher.publishEvent(new UserDeletedEvent(userId));
}
```

### 4.3 이벤트 클래스 정의

`decapet-official/backend/src/main/java/com/backend/domain/user/event/UserDeletedEvent.java:1-3`

```java
// record로 정의하는 도메인 이벤트
public record UserDeletedEvent(String userId) {}
```

### 4.4 트랜잭션 외부 실행이 필요한 경우

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:79-95`

```java
// SMS 발송처럼 외부 서비스 호출은 트랜잭션 커밋 완료 후 실행해야 하는 경우
// TransactionTemplate으로 DB 작업 경계를 명시적으로 분리
public void sendPhoneChangeSms(String userId, String phone) {
    User user = userRepository.getByIdAndDeletedAtIsNull(userId);
    userRepository.validatePhoneNotDuplicate(phone, user.getPhone());

    String code = transactionTemplate.execute(status -> {
        verificationRepository.deleteByPhone(phone);
        String verificationCode = generateVerificationCode();
        Verification verification = verificationMapper.toEntity(
                new VerificationCreateInfo(phone, verificationCode, VERIFICATION_TTL_MINUTES));
        verificationRepository.save(verification);
        return verificationCode;
    });

    smsService.sendVerificationCode(phone, code); // 트랜잭션 외부 실행
}
```

### 4.5 커스텀 예외 사용

`decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:109-114`

```java
@Transactional
public void changePassword(String userId, String accessToken, String currentPassword, String newPassword) {
    User user = userRepository.getByIdAndDeletedAtIsNull(userId);

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new InvalidPasswordException(); // BusinessException 하위 커스텀 예외
    }

    user.updatePassword(passwordEncoder.encode(newPassword));
    tokenService.blacklistAccessToken(accessToken);
    tokenService.invalidateAllTokensByUserId(userId);
}
```

```java
// 예외 클래스 정의 패턴
// com.backend.domain.user.exception.UserNotFoundException
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
```

---

## 5. 체크리스트

### 클래스 선언
- [ ] `@Service` 선언
- [ ] `@RequiredArgsConstructor` 선언 (필드 주입 없음)
- [ ] 클래스 레벨 `@Transactional` 선언
- [ ] 서비스 인터페이스 분리 (`{Domain}Service` + `{Domain}ServiceImpl`)

### 트랜잭션
- [ ] 모든 조회 전용 메서드에 `@Transactional(readOnly = true)` 명시
- [ ] 쓰기 메서드는 클래스 레벨 트랜잭션 상속 또는 메서드 레벨 재선언
- [ ] 외부 서비스 호출이 포함된 경우 `TransactionTemplate` 또는 이벤트로 경계 분리

### 도메인 로직
- [ ] 엔티티 조회 시 `getByXxx()` default 메서드 사용
- [ ] 검증 로직은 `validateXxx()` 메서드 또는 private 메서드로 분리
- [ ] 엔티티 상태 변경은 도메인 메서드 호출로만 처리 (setter 직접 호출 금지)
- [ ] 트랜잭션 후 cascade 작업은 `ApplicationEventPublisher`로 이벤트 발행

### 예외 처리
- [ ] 모든 예외는 `BusinessException` 하위 커스텀 예외 사용
- [ ] 예외 클래스는 `com.backend.domain.{x}.exception` 패키지에 위치
- [ ] `ErrorCode` enum에 에러 코드/메시지/HTTP 상태 정의 후 사용

### 코드 품질
- [ ] 컨트롤러 레이어 코드(응답 변환, HTTP 상태 판단) 없음
- [ ] 메서드 하나가 단일 비즈니스 시나리오만 처리
- [ ] `@Transactional` 없는 public 메서드는 의도적임을 주석으로 명시
