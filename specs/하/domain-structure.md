# domain-structure.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 개요

decapet-official/backend 는 `com.backend.domain` 하위에 비즈니스 컨텍스트 단위로 패키지를 분리한다.
각 도메인은 독립적인 폴더로 존재하며, 도메인 내부 구조는 팀 컨벤션에 따라 자유롭게 구성할 수 있다.

---

## 원칙

1. 기능별로 도메인 폴더를 분리한다. `user`, `pet`, `order` 등 비즈니스 단위가 패키지 단위다.
2. 한 패키지가 두 개 이상의 비즈니스 컨텍스트를 담지 않는다.

---

## 강제 사항

### must

- 도메인은 `com.backend.domain.{도메인명}/` 형태로 분리해야 한다.
- 도메인 내에 controller, service, repository 역할을 하는 클래스가 존재해야 한다.
- 모든 엔티티는 `com.backend.global.common.BaseEntity` 를 상속해야 한다.

---

## 예시 (decapet 인용)

### 도메인 패키지 목록 (일부)

```
com.backend.domain.user/
com.backend.domain.pet/
com.backend.domain.order/
com.backend.domain.payment/
com.backend.domain.appointment/
com.backend.domain.medicine/
com.backend.domain.board/
com.backend.domain.cart/
...
```

### BaseEntity — 모든 엔티티의 부모

```java
// com.backend.global.common.BaseEntity
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

    @Id
    @Column(length = 26)
    private String id;  // ULID 자동 부여

    private LocalDateTime createdAt;
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

---

## 체크리스트

- [ ] 도메인이 `com.backend.domain.{도메인명}/` 으로 분리되어 있다
- [ ] 각 도메인에 controller, service, repository 역할의 클래스가 있다
- [ ] 모든 엔티티가 `BaseEntity` 를 상속한다
- [ ] 단일 패키지가 두 개 이상의 비즈니스 컨텍스트를 담고 있지 않다
