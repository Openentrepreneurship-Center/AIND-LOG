# domain-structure.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 `com.backend.domain` 하위에 비즈니스 컨텍스트 단위로 도메인을 분리한다.
각 도메인 패키지는 표준 내부 폴더 구조를 갖추고, 도메인 간 참조는 service 계층을 통해서만 허용한다.
공유 타입은 `com.backend.global.common/` 에 위치하며, 모든 엔티티는 ULID PK 를 사용하는
`BaseEntity` 를 상속한다.

---

## 원칙

1. 도메인 패키지는 비즈니스 컨텍스트 1개에 대응한다.
2. 도메인 내부 레이어(controller → service → repository)는 동일 도메인 안에서 완결된다.
3. Cross-domain 참조는 상대 도메인의 service 를 주입하는 방식으로만 허용한다.
4. 공유 타입은 `com.backend.global.common/` 에만 정의한다.
5. 모든 엔티티는 `BaseEntity` 를 상속하며 ULID PK 를 사용한다.

---

## 강제 사항

### must

- 각 도메인 패키지는 다음 표준 폴더를 포함해야 한다:
  ```
  controller/  service/  repository/  entity/
  dto/request/  dto/response/  dto/mapper/
  exception/
  ```
- Cross-domain 참조는 반드시 상대 도메인의 service 를 주입해야 한다. 타 도메인 repository 직접 주입 금지.
- 모든 엔티티는 `com.backend.global.common.BaseEntity` 를 상속해야 한다.
- 엔티티 PK 는 ULID 문자열이어야 한다.

### should

- 공유 타입은 `com.backend.global.common/` 에 위치시킨다.
- Soft-delete 대상 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 을 적용한다.
- 도메인 간 연쇄 부작용은 이벤트(`ApplicationEventPublisher`)로 전달한다.

---

## 예시 (decapet 인용)

### pet 도메인 구조

```
com.backend.domain.pet/
  controller/
    PetController.java
    PetApi.java
    AdminPetController.java
    AdminPetApi.java
  service/
    PetService.java
    AdminPetService.java
  repository/
    PetRepository.java
    PetVetRepository.java
  entity/
    Pet.java
    PetVet.java
    Gender.java       -- enum
    VetPosition.java  -- enum
  dto/
    request/
      PetRegisterRequest.java
      PetUpdateRequest.java
    response/
      PetResponse.java
      PetVetResponse.java
    internal/
      PetRegisterInfo.java
      PetUpdateInfo.java
    mapper/
      PetMapper.java
      PetResponseMapper.java
  exception/
    PetNotFoundException.java
    PetPermissionDeniedException.java
    InvalidBirthdateException.java
    WeightUpdateRestrictedException.java
```

### BaseEntity 상속

```java
// com.backend.global.common.BaseEntity
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

    @Id
    @Column(length = 26)
    private String id;  // ULID 자동 부여

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

### Cross-domain — service 호출 패턴

```java
// com.backend.domain.pet.service.PetService (예시)
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;  // 자신의 repository
    private final UserService userService;      // 타 도메인 → service 만 허용
    // private final UserRepository userRepo;  // 금지
}
```

---

## 체크리스트

- [ ] 모든 도메인이 `com.backend.domain.{도메인명}/` 형태로 분리되어 있다
- [ ] 각 도메인에 `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `exception/` 이 있다
- [ ] 타 도메인 repository 를 직접 주입하는 코드가 없다
- [ ] 공유 타입이 `com.backend.global.common/` 에만 위치한다
- [ ] 모든 엔티티가 `BaseEntity` 를 상속하고 ULID PK 를 사용한다
- [ ] 도메인 예외 클래스가 `exception/` 하위에 있고 `BusinessException` 을 상속한다
- [ ] Soft-delete 대상 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 이 적용된다
