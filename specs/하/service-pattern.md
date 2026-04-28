# service-pattern.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

하 등급은 서비스 클래스의 최소 요건만 정의한다.
단일 구현체, 클래스 레벨 `@Transactional` 한 개 적용, 예외 처리 방식은 `RuntimeException` 직접 throw도 허용한다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상/중 등급 | 하 등급 |
|------|-----------|---------|
| 인터페이스 분리 | 선택/필수 | 불필요 |
| `@Transactional` 메서드별 명시 | 필수 | 클래스 레벨 1개로 대체 허용 |
| `BusinessException` 상속 | 필수 | `RuntimeException` 직접 throw 허용 |
| 검색/쓰기 메서드 분리 | 권장 | 선택 |

---

## 3. 강제 사항

### must
- `@Service` 선언
- 클래스 레벨 `@Transactional` 또는 메서드 레벨 `@Transactional` 중 하나 이상 적용
- repository 조회 실패 시 예외를 던질 것 (silent failure 금지)

### should
- `@RequiredArgsConstructor` 생성자 주입
- 조회 메서드에 `@Transactional(readOnly = true)` 적용
- `BusinessException` 상속 커스텀 예외 사용

---

## 4. 예시 코드

최소 구현 예시:

```java
// com.backend.domain.user.service.UserService 패턴 참고
@Service
@Transactional
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    @Transactional(readOnly = true)
    public ExampleResponse getExample(String id) {
        return exampleRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("not found"));
    }
}
```

---

## 5. 체크리스트

- [ ] `@Service` 선언되었는가
- [ ] 트랜잭션 어노테이션이 최소 1개 이상 적용되었는가
- [ ] repository 조회 실패 시 예외를 던지는가
- [ ] 서비스에서 비즈니스 로직을 담당하는가 (컨트롤러에 로직 없음)
