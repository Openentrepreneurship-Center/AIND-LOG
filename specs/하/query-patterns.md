# query-patterns.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

---

## 1. 개요

이 프로젝트는 JPA + Spring Data만 사용한다. xml 기반 쿼리 파일과 `#{}` 바인딩은 존재하지 않는다.
하 등급은 derived query 사용만 강제하며, 페이징·N+1 방지는 권장 수준이다.

---

## 2. 변경/완화 사항 (상 등급 대비)

| 항목 | 상/중 등급 | 하 등급 |
|------|-----------|---------|
| N+1 방지 (`@EntityGraph`) | 필수 | 권장 |
| 페이징 (`Pageable`) | 필수 | 권장 |
| 동적 검색 (`Specification`) | 권장 | 선택 |
| `@Modifying clearAutomatically` | 필수 | 권장 |
| 사용자 경계 조건 | 필수 | 권장 |

---

## 3. 강제 사항

### must
- 데이터 조회는 `JpaRepository` derived query(`findByXxx`) 사용
- xml 쿼리 파일 사용 금지
- 조회 실패 시 예외 throw (silent null 반환 금지)

### should
- 연관 엔티티 조회 시 `@EntityGraph` 또는 fetch join으로 N+1 방지
- 목록 조회는 `Pageable` + `Page<T>` 사용
- 단건 반환 타입은 `Optional<T>` 사용

---

## 4. 예시 코드

```java
// com.backend.domain.user.repository.UserRepository 패턴 참고
public interface ExampleRepository extends JpaRepository<Example, String> {

    // derived query
    Optional<Example> findByIdAndDeletedAtIsNull(String id);
    List<Example> findByOwnerId(String ownerId);
    boolean existsByNameAndDeletedAtIsNull(String name);

    // 페이징 (권장)
    Page<Example> findByOwnerIdAndDeletedAtIsNull(String ownerId, Pageable pageable);
}
```

---

## 5. 체크리스트

- [ ] derived query(`findByXxx`) 네이밍 규칙을 사용하는가
- [ ] xml 쿼리 파일이 없는가
- [ ] 조회 실패 시 예외를 던지는가 (null 반환 금지)
- [ ] 단건 반환 타입이 `Optional<T>`인가
