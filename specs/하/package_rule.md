# package_rule.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지의 최상위 분리 원칙을 정의한다.
도메인 코드와 전역 인프라 코드를 두 영역으로 구분한다.

---

## 2. 최상위 구조 (must)

```
com.backend
├── domain      # 비즈니스 도메인 (com.backend.domain.{x})
├── global      # 전역 공통 인프라
└── BackendApplication.java
```

- 도메인 코드는 반드시 `com.backend.domain.{x}` 아래에 위치한다.
- 전역 설정·공통 유틸·에러 처리는 `com.backend.global` 아래에 위치한다.

---

## 3. 도메인 내부 기본 구조

```
com.backend.domain.{x}
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
└── exception
```

모든 JPA Entity는 `com.backend.global.common.BaseEntity`를 상속한다.

```java
// decapet-official/backend/src/main/java/com/backend/global/common/BaseEntity.java:30-34
public abstract class BaseEntity implements Persistable<String> {
    @Id
    @Column(length = 26)
    private String id;  // ULID 자동 생성
}
```

---

## 4. 체크리스트

- [ ] 도메인 코드가 `com.backend.domain.{x}` 아래에 위치하는가
- [ ] 전역 코드가 `com.backend.global` 아래에 위치하는가
- [ ] 모든 JPA Entity가 `BaseEntity`를 상속하는가
- [ ] 신규 예외 클래스가 `domain.{x}.exception` 패키지에 위치하는가
