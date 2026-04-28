# coding-standards.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 패키지에서 지켜야 할 핵심 코딩 기준을 정의한다.
생성자 주입, 명확한 네이밍, 기본 포매팅 준수가 핵심이다.

---

## 2. 강제 사항

### 2.1 의존성 주입 (must)
- 필드 주입(`@Autowired`)은 사용하지 않는다.
- `@RequiredArgsConstructor` + `private final` 생성자 주입만 허용한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/service/UserService.java:28-32
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
```

### 2.2 네이밍 (must)
- 클래스명은 PascalCase, 역할을 명확히 표현한다.
- 메서드·변수명은 camelCase, 동작·의미를 명확히 표현한다.
- 약어 남용을 피하고, 의미 전달이 불분명한 이름을 사용하지 않는다.

### 2.3 포매팅 (must)
- 들여쓰기: 스페이스 4칸
- import 와일드카드(`*`) 금지
- 불필요한 공백 줄 최소화

### 2.4 공통 응답 (must)
- 성공 응답은 `SuccessResponse.of(SuccessCode, data)` 형태를 사용한다.

```java
// decapet-official/backend/src/main/java/com/backend/domain/user/controller/UserController.java:38-39
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_GET_SUCCESS, response));
```

### 2.5 예외 처리 (must)
- 비즈니스 예외는 `BusinessException` 하위 타입으로 정의하고 `ErrorCode`를 포함한다.

---

## 3. 체크리스트

- [ ] `@RequiredArgsConstructor` + `private final` 생성자 주입만 사용하는가
- [ ] 클래스·메서드·변수명이 의미를 명확히 표현하는가
- [ ] import 와일드카드가 없는가
- [ ] 성공 응답이 `SuccessResponse.of(...)` 형태인가
- [ ] 비즈니스 예외가 `BusinessException` 하위 타입인가
