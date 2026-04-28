# domain-structure.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 비즈니스 컨텍스트 단위로 도메인을 분리한다.
`com.backend.domain` 하위에 23개 도메인(`user`, `pet`, `order`, `payment`, `appointment`, `medicine`,
`board`, `cart`, `medicinecart`, `product`, `customproduct`, `prescription`, `delivery`, `schedule`,
`remotearea`, `spam`, `sitesetting`, `terms`, `admin`, `auth`, `breed`, `banner`, `notification`)이 위치하며,
각 도메인은 내부적으로 표준 9종 하위 패키지를 갖는다.
도메인 간 결합은 service 호출과 도메인 이벤트로만 허용하며, 타 도메인의 repository 를 직접 주입하지 않는다.
공유 타입·유틸·설정은 `com.backend.global` 에만 위치한다.

---

## 원칙

1. 도메인 = 하나의 비즈니스 컨텍스트. `user` 와 `pet` 은 별개 도메인이며, 하나의 도메인 패키지가 두 컨텍스트를 겸하지 않는다.
2. 도메인 내 모든 레이어(controller → service → repository)는 동일 도메인 패키지 안에서만 완결된다.
3. Cross-domain 의존은 service 레이어 호출만 허용한다. 타 도메인 repository / entity 를 직접 주입하면 안 된다.
4. 도메인 삭제·상태 변경 등 연쇄 부작용은 도메인 이벤트(`ApplicationEventPublisher`)로 전달하여 결합도를 낮춘다.
5. 공유가 필요한 타입(페이지 응답, 공통 DTO)은 `com.backend.global.common` 에만 정의한다.
6. 모든 엔티티는 `com.backend.global.common.BaseEntity` 를 상속하며, PK 는 ULID 문자열로 자동 부여된다.
7. Soft-delete 는 `BaseEntity.delete()` 호출로 처리하며, `@SQLRestriction("deleted_at IS NULL")` 으로 조회 시 자동 필터링한다.

---

## 강제 사항

### must

- 도메인 패키지는 비즈니스 컨텍스트 1개에 대응해야 한다.
- 각 도메인 패키지는 아래 9종 하위 패키지를 기준으로 구성해야 한다:
  ```
  controller/   - REST 컨트롤러 (+ Api 인터페이스로 Swagger 분리)
  service/      - 비즈니스 로직
  repository/   - Spring Data JPA Repository
  entity/       - JPA 엔티티 및 enum
  dto/
    request/    - 입력 DTO
    response/   - 출력 DTO
    internal/   - 도메인 내부 전달 DTO (레이어 간)
    mapper/     - DTO ↔ Entity 변환
  exception/    - BusinessException 상속 예외 클래스
  event/        - 도메인 이벤트 레코드 및 리스너
  mapper/       - 도메인 수준 매퍼 (dto/mapper 와 별도 운용 시)
  ```
- Cross-domain 의존은 반드시 상대 도메인의 service 를 주입하는 방식으로만 구현해야 한다. 타 도메인 repository 직접 주입 금지.
- 공유 타입은 `com.backend.global.common/` 에만 위치해야 한다. 도메인 패키지 내 공유 타입 금지.
- 모든 엔티티는 `com.backend.global.common.BaseEntity` 를 상속해야 한다.
- 엔티티 PK 는 ULID(`UlidGenerator.generate()`)로 생성해야 한다. 숫자형 자동증가(auto_increment) PK 금지.
- 도메인 삭제·탈퇴 같은 연쇄 부작용은 `ApplicationEventPublisher` + 이벤트 레코드로 전달해야 한다.

### should

- `controller/` 내 컨트롤러와 Swagger API 인터페이스를 파일로 분리한다 (`PetController` / `PetApi`).
- `dto/internal/` 을 두어 service 레이어 간 DTO 와 외부 요청/응답 DTO 를 명확히 구분한다.
- 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 을 적용하여 soft-delete 된 행이 조회에 포함되지 않도록 한다.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 를 엔티티에 적용하여 무인자 생성을 제한한다.

---

## 예시 (decapet 인용)

### 디렉터리 구조 — user 도메인

```
com.backend.domain.user/
  controller/
    UserController.java
    UserApi.java            -- Swagger @Operation/@Tag 인터페이스
    AdminUserController.java
    AdminUserApi.java
  service/
    UserService.java
    AdminUserService.java
  repository/
    UserRepository.java
    UserTermConsentRepository.java
  entity/
    User.java
    UserTermConsent.java
    PermissionType.java     -- enum
    TermConsent.java        -- enum
  dto/
    request/
      UpdateProfileRequest.java
      ChangePasswordRequest.java
    response/
      UserResponse.java
      AdminUserListResponse.java
    internal/
      ProfileUpdateInfo.java
    mapper/
      UserMapper.java
      UserResponseMapper.java
      AdminUserResponseMapper.java
  exception/
    UserNotFoundException.java
    DuplicateEmailException.java
    InvalidPasswordException.java
    UserAccountLockedException.java
  event/
    UserDeletedEvent.java
    UserDeletionEventListener.java
```

### BaseEntity 상속 및 ULID PK

```java
// com.backend.global.common.BaseEntity
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

    @Id
    @Column(length = 26)
    private String id;              // ULID 자동 부여

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    protected BaseEntity() {
        this.id = UlidGenerator.generate();
    }

    public void delete() {
        this.deletedAt = DateTimeUtil.now();
    }
}
```

### User 엔티티 — BaseEntity 상속, soft-delete

```java
// com.backend.domain.user.entity.User
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {
    // ...
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Pet> pets = new HashSet<>();
    // ...
}
```

### 도메인 이벤트 — UserDeletedEvent

```java
// com.backend.domain.user.event.UserDeletedEvent
public record UserDeletedEvent(String userId) {}

// UserDeletionEventListener: pet, order 등 연관 도메인의 service 를 주입해 처리
// 타 도메인 repository 를 직접 주입하지 않는다
```

### Cross-domain — service 호출 패턴

```java
// com.backend.domain.pet.service.PetService (예시)
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;       // 자신의 repository
    private final UserService userService;           // 타 도메인 → service 호출만 허용
    // private final UserRepository userRepository; // 금지: 타 도메인 repository 직접 주입 금지
}
```

---

## 체크리스트

- [ ] 모든 도메인 패키지가 `com.backend.domain.{도메인명}/` 형태로 존재한다
- [ ] 각 도메인에 `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `exception/` 이 있다
- [ ] `event/` 패키지가 있고 연쇄 부작용을 이벤트로 전달한다
- [ ] 타 도메인 repository 를 직접 주입하는 코드가 없다 (`grep -r "import com.backend.domain.*.repository" --include="*.java"` 로 교차 확인)
- [ ] 공유 DTO / 타입이 `com.backend.global.common/` 에만 위치한다
- [ ] 모든 엔티티가 `BaseEntity` 를 상속한다
- [ ] 모든 엔티티 PK 가 ULID 문자열이다 (`@GeneratedValue` 금지 확인)
- [ ] Soft-delete 대상 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 이 적용된다
- [ ] 도메인 예외 클래스가 `exception/` 하위에 위치하고 `BusinessException` 을 상속한다
- [ ] Controller 파일과 Swagger Api 인터페이스 파일이 분리되어 있다
