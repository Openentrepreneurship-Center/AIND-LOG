# domain-structure.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 도메인 폴더 분리라는 최소 구조 요건만 강제한다.
`com.backend.domain.{domain}` 경로에 도메인별 패키지를 두는 것이 유일한 필수 규칙이다.
내부 폴더 구성(controller/service/repository 등)은 자율이며, cross-domain repository 직접 호출도 허용한다.
단, 장기적으로 중 등급 구조로 개선하는 것을 목표로 한다.

---

## 2. 변경/완화 사항

| 항목 | 중 등급 원칙 | 하 등급 완화 |
|------|------------|------------|
| 내부 폴더 구성 | `controller, service, repository, entity, dto, exception, event` 표준 강제 | 자율 (비즈니스 요구에 따라 결정) |
| Cross-domain repository 호출 | service 경유 강제, repository 직접 접근 금지 | repository 직접 주입 허용 |
| 공유 타입 위치 | `com.backend.global.common` 단일 위치 강제 | 위치 자율 (단, 중복 선언 지양) |
| 도메인 이벤트 | 권장 | 해당 없음 |

---

## 3. 강제 사항

### must

- 각 도메인 코드는 `com.backend.domain.{domain}` 패키지 아래에 위치해야 한다.
- 도메인 경계 외부(global, config 등)에 특정 도메인 비즈니스 로직을 위치시켜서는 안 된다.
- 컨트롤러에서 데이터베이스 접근 코드를 직접 작성해서는 안 된다 (최소한 service 또는 repository 레이어 분리).

### should

- 내부 패키지를 `controller`, `service`, `repository`, `entity`, `dto` 기준으로 분리하는 것을 권장한다.
- 중 등급 구조 도입 전 중간 단계로 `repository` 직접 접근을 사용할 수 있으나, 향후 service 경유로 리팩터링한다.

---

## 4. 예시 코드

### 4.1 최소 도메인 패키지 구조

```
com.backend.domain/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java    # Spring Data JPA
│   └── User.java              # @Entity
├── pet/
│   ├── PetController.java
│   ├── PetService.java
│   ├── PetRepository.java
│   └── Pet.java
└── order/
    ├── OrderController.java
    ├── OrderService.java
    ├── OrderRepository.java
    └── Order.java
```

### 4.2 하 등급 허용 패턴 — repository 직접 주입 (임시 허용)

```java
// 하 등급에서 허용 — 단, 중 등급으로 개선 예정임을 주석으로 명시
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    // TODO: 중 등급 전환 시 UserService / PetService 경유로 변경
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public void create(String userId, String petId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        petRepository.findById(petId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
        appointmentRepository.save(new Appointment(userId, petId));
    }
}
```

---

## 5. 체크리스트

- [ ] 도메인 코드가 `com.backend.domain.{domain}` 패키지에 위치하는가?
- [ ] 도메인 비즈니스 로직이 `global` 또는 `config` 패키지에 섞여 있지 않은가?
- [ ] 컨트롤러에서 데이터베이스 접근 코드(repository/entity)를 직접 작성하지 않는가?
- [ ] cross-domain repository 직접 접근 코드에 TODO 주석(향후 service 경유 전환 예정)이 달려 있는가?
