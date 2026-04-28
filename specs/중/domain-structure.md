# domain-structure.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

본 문서는 `com.backend.domain` 하위 패키지 구조와 도메인 간 접근 정책을 정의한다.
decapet-official 백엔드는 21개 비즈니스 도메인으로 구성되며, 각 도메인은 독립적인 패키지 컨텍스트를 가진다.
도메인 경계를 명확히 하여 변경 영향 범위를 격리하고 테스트 용이성을 높이는 것이 목적이다.
JPA 기반이므로 Mapper XML 파일은 존재하지 않으며, `repository` 계층이 그 역할을 대신한다.
공유 타입은 `com.backend.global.common` 패키지 한 곳으로 통일한다.

---

## 2. 변경/완화 사항

| 항목 | 상 등급 원칙 | 중 등급 적용 |
|------|------------|------------|
| 데이터 접근 계층 | `mapper` 패키지 (XML 기반) | `repository` 패키지 (Spring Data JPA) |
| 공유 타입 위치 | `domain/common` 또는 `global/common` 중 선택 | `com.backend.global.common` 단일 위치로 고정 |
| Cross-domain Mapper 직접 호출 금지 | Mapper 직접 접근 금지 | Repository 직접 접근 금지 (동일 원칙 유지) |
| dto 내부 mapper | 별도 명시 없음 | `dto/mapper` 하위 패키지 허용 (DTO ↔ Entity 변환 전용) |
| dto 내부 internal | 별도 명시 없음 | `dto/internal` 하위 패키지 허용 (도메인 내부 전용 DTO) |

---

## 3. 강제 사항

### must

- 각 도메인은 `com.backend.domain.{domain}` 패키지 아래에 독립적으로 위치해야 한다.
- 도메인 내부에는 `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `event` 패키지를 기준으로 구성한다.
- `dto` 하위는 `request`, `response`를 기본으로 하며, 필요 시 `mapper`, `internal`을 추가할 수 있다.
- 타 도메인의 `repository`를 직접 주입하여 사용하는 것은 금지한다. 반드시 해당 도메인의 `service`를 통해 접근한다.
- 컨트롤러에서 `repository`를 직접 주입하는 것은 금지한다.
- 글로벌 공유 컴포넌트(BaseEntity, 공통 enum, 유틸 등)는 `com.backend.global.common` 또는 `com.backend.global.util`에만 위치시킨다.
- 도메인 예외 클래스는 해당 도메인의 `exception` 패키지에 위치시킨다.

### should

- 도메인 간 결합이 불가피한 쓰기 작업은 도메인 이벤트(`event` 패키지) 또는 해당 도메인 서비스 호출을 통해 처리하기를 권장한다.
- 읽기 전용 조회 최적화가 필요하면 동일 도메인 내 `repository` 직접 호출은 서비스 내부에서만 허용한다.
- 순환 의존성이 감지되면 이벤트 기반 분리(`ApplicationEventPublisher`)를 우선 검토한다.

---

## 4. 패키지 구조

### 4.1 전체 구조

```
com.backend/
├── global/
│   ├── common/          # BaseEntity, ULID 생성기, 공통 응답 등
│   ├── config/          # SecurityConfig, JPA 설정 등
│   ├── error/           # GlobalExceptionHandler, ErrorCode, ErrorResponse
│   │   └── exception/   # BusinessException 및 글로벌 예외 클래스
│   ├── filter/          # JwtFilter, AccountValidationFilter, PermissionFilter
│   ├── security/        # CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
│   └── util/            # DateTimeUtil, UlidGenerator 등
└── domain/
    ├── user/
    ├── pet/
    ├── order/
    ├── payment/
    ├── appointment/
    ├── medicine/
    ├── board/
    └── ...              # 그 외 도메인
```

### 4.2 도메인 내부 표준 구조

실제 `com.backend.domain.user` 도메인을 기준으로 한 표준 구조:

```
com.backend.domain.{domain}/
├── controller/
│   └── {Domain}Controller.java
├── service/
│   ├── {Domain}Service.java          # 인터페이스 또는 구현 클래스
│   └── {Domain}ServiceImpl.java      # 인터페이스 분리 시
├── repository/
│   └── {Domain}Repository.java       # Spring Data JPA Repository
├── entity/
│   └── {Domain}.java                 # @Entity, BaseEntity 상속
├── dto/
│   ├── request/
│   │   ├── {Domain}CreateRequest.java
│   │   └── {Domain}UpdateRequest.java
│   ├── response/
│   │   └── {Domain}Response.java
│   ├── mapper/                       # DTO ↔ Entity 변환 (선택)
│   │   └── {Domain}DtoMapper.java
│   └── internal/                     # 도메인 내부 전용 DTO (선택)
│       └── {Domain}InternalDto.java
├── exception/
│   └── {Domain}NotFoundException.java  # BusinessException 상속
└── event/
    └── {Domain}DeletedEvent.java       # 도메인 이벤트 (선택)
```

---

## 5. 예시 코드

### 5.1 올바른 cross-domain 접근 — service 경유

```java
// com.backend.domain.appointment.service.AppointmentService
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;   // 타 도메인: service 주입 (허용)
    private final PetService petService;     // 타 도메인: service 주입 (허용)

    public void createAppointment(String userId, String petId, CreateAppointmentRequest req) {
        // 타 도메인 조회는 service를 통해
        userService.validateUserExists(userId);
        petService.validatePetOwnership(petId, userId);

        Appointment appointment = new Appointment(userId, petId, req.getScheduleId());
        appointmentRepository.save(appointment);
    }
}
```

### 5.2 금지 패턴 — 타 도메인 repository 직접 주입

```java
// 금지
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final UserRepository userRepository;   // 금지: 타 도메인 repository 직접 주입
    private final PetRepository petRepository;     // 금지: 타 도메인 repository 직접 주입
}
```

### 5.3 BaseEntity 상속 패턴

```java
// com.backend.domain.pet.entity.Pet
@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet extends BaseEntity {   // com.backend.global.common.BaseEntity

    @Column(nullable = false)
    private String name;

    private LocalDate birthDate;

    // soft-delete: BaseEntity.delete() 호출
    public void softDelete() {
        delete();
    }
}
```

### 5.4 도메인 이벤트 발행 (결합도 낮추기)

```java
// com.backend.domain.user.event.UserDeletedEvent
public record UserDeletedEvent(String userId) {}

// 서비스에서 발행
@Service
@RequiredArgsConstructor
public class UserService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }
}
```

---

## 6. 체크리스트

- [ ] 도메인 패키지가 `com.backend.domain.{domain}` 경로에 위치하는가?
- [ ] 도메인 내부에 `controller`, `service`, `repository`, `entity`, `dto` 패키지가 존재하는가?
- [ ] 타 도메인의 `repository`를 직접 주입하는 코드가 없는가?
- [ ] 컨트롤러에서 `repository` 또는 `entity`를 직접 참조하는 코드가 없는가?
- [ ] 글로벌 공유 타입이 `com.backend.global.common` 이외의 위치에 분산되어 있지 않은가?
- [ ] 도메인 예외 클래스가 해당 도메인의 `exception` 패키지에 위치하는가?
- [ ] 모든 `@Entity` 클래스가 `BaseEntity`를 상속하고 ULID PK를 사용하는가?
- [ ] soft-delete 대상 엔티티가 `BaseEntity.delete()`를 호출하는가?
- [ ] 순환 의존성이 없는가 (A→B→A 패턴 금지)?
- [ ] 도메인 간 쓰기 연산이 이벤트 또는 서비스 호출로 위임되는가?
