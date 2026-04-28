# coding-standards.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend 프로젝트에서 중 등급 작업자가 따라야 할 코딩 원칙을 정의한다.
SOLID 원칙 전체를 권장하되 강제하지는 않는다.
생성자 주입, record DTO, 필드 선언 위치, import 정리는 이 등급에서도 강제한다.
상 등급 원본은 MyBatis 기반이나, 본 프로젝트는 JPA + Spring Data를 사용하므로 Mapper XML 관련 항목은 적용하지 않는다.

## 변경/완화 사항

- 상 등급의 SOLID 5원칙 전부 강제 → 중 등급은 SRP·DIP 중심으로 권장 수준으로 완화
- 상 등급의 "인터페이스와 추상화 활용(OCP)" → 단일 구현체이면 인터페이스 생략 허용
- 상 등급의 "Mock 가능한 인터페이스 설계" → `@SpringBootTest` 또는 Testcontainers 사용으로 대체 가능
- MyBatis Mapper / XML 관련 코딩 표준은 해당 없음 (JPA + Spring Data 사용)

## 이 등급에서 강제하는 것

**must**
- 생성자 주입만 사용한다. Lombok `@RequiredArgsConstructor`로 선언한다.
- 필드 선언은 클래스 최상단에 둔다. `static final` 상수 → 인스턴스 `final` 필드 순서를 지킨다.
- Request / Response DTO는 Java `record`로 선언한다.
- `import`는 와일드카드(`*`) 없이 개별 지정한다. 사용하지 않는 import는 제거한다.
- 예외는 `BusinessException`을 상속한 도메인 전용 예외로 던진다. `RuntimeException`을 직접 던지지 않는다.
- 검증 상수는 `ValidationConstants`에 정의하고 `@Pattern(regexp = ValidationConstants.XXX_REGEX)` 형태로 참조한다.

**should**
- 각 클래스는 하나의 책임을 갖도록 설계한다 (SRP 권장).
- 고수준 모듈(Service)은 저수준 구현(Repository 구현체)에 직접 의존하지 않도록 인터페이스를 통해 접근한다 (DIP 권장).
- 주석은 "왜(Why)"에 집중하고, "무엇을(What)"은 코드 자체로 표현한다.
- 불필요한 객체 생성을 피한다.

## 예시 코드

생성자 주입 + `@RequiredArgsConstructor` (domain/user/service/UserService.java:31-48):

```java
// com.backend.domain.user.service.UserService
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int VERIFICATION_TTL_MINUTES = 5;  // 상수: 최상단

    private final UserRepository userRepository;             // 필드: 최상단
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);
        return userResponseMapper.toResponse(user);
    }
}
```

record DTO + ValidationConstants 참조 (domain/user/dto/request/PhoneVerifyRequest.java:9-20):

```java
// com.backend.domain.user.dto.request.PhoneVerifyRequest
public record PhoneVerifyRequest(
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        String phone,

        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {}
```

## 체크리스트

- [ ] `@Autowired` 필드 주입 또는 수정자 주입을 사용하지 않는가?
- [ ] `@RequiredArgsConstructor`를 사용하고 모든 주입 필드가 `final`인가?
- [ ] 필드 선언이 클래스 최상단(static 상수 → 인스턴스 필드 순)에 위치하는가?
- [ ] Request / Response DTO가 `record`로 선언되었는가?
- [ ] import에 와일드카드(`*`)가 없고 미사용 import가 제거되었는가?
- [ ] 예외를 `RuntimeException`으로 직접 던지지 않고 `BusinessException` 계열을 사용하는가?
- [ ] 검증 정규식이 `ValidationConstants` 상수를 참조하는가?
- [ ] `@Transactional(readOnly = true)`를 조회 메서드에 적용하였는가?
- [ ] 메서드 하나가 한 가지 역할만 수행하는가?
