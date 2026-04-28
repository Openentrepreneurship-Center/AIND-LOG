# mapper-pattern.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

이 프로젝트는 JPA + Spring Data만 사용한다.
xml 기반 쿼리 파일(`Mapper.xml`, 파라미터 바인딩 `#{}`, `<foreach>` 등)은 사용하지 않는다.
Repository는 `JpaRepository<Entity, String>` 직접 상속만 필수이며, Entity-DTO 변환 방식은 자율이다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상/중 등급 | 하 등급 |
|------|-----------|---------|
| `getByXxx()` default 메서드 | 필수 | 권장 |
| `@Component *Mapper` 클래스 분리 | 필수 | `@Component`, static, 생성자 변환 모두 허용 |
| 검증 default 메서드 | 권장 | 선택 |
| `JpaSpecificationExecutor` 병행 상속 | 권장 | 선택 |

---

## 3. 강제 사항

### must
- `JpaRepository<Entity, String>` 상속 (ULID PK이므로 두 번째 타입은 `String`)
- Entity↔DTO 변환 로직은 서비스나 컨트롤러에 인라인 작성 금지 — 별도 변환 수단 분리

### should
- `default getByXxx()` 메서드로 조회 실패 예외 처리
- `@Component *Mapper` 클래스 + `toEntity()` / `toResponse()` 명시적 메서드

---

## 4. 예시 코드

```java
// com.backend.domain.user.repository.UserRepository 패턴 참고
public interface ExampleRepository extends JpaRepository<Example, String> {

    Optional<Example> findByIdAndDeletedAtIsNull(String id);

    default Example getById(String id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(ExampleNotFoundException::new);
    }
}
```

```java
// com.backend.domain.user.dto.mapper.UserResponseMapper 패턴 참고
@Component
public class ExampleMapper {
    public ExampleResponse toResponse(Example example) {
        return new ExampleResponse(example.getId(), example.getName());
    }
}
```

---

## 5. 체크리스트

- [ ] `JpaRepository<Entity, String>` 상속이 되어 있는가
- [ ] Entity↔DTO 변환이 서비스/컨트롤러 외부로 분리되어 있는가
- [ ] xml 쿼리 파일이 없는가
- [ ] 조회 실패 시 예외를 던지는가
