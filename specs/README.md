# Decapet-Official Backend 코딩 스펙 (상 / 중 / 하)

본 디렉토리는 `decapet-official/backend` 프로젝트에 적용할 **세 단계 코딩 스펙** 모음이다. 각 등급은 다른 등급과 독립적으로 적용 가능하며, 한 폴더의 문서들만 읽고도 해당 등급으로 코드를 작성·리뷰할 수 있다.

| 등급 | 위치 | 적용 상황 |
| --- | --- | --- |
| 상 | `specs/상/` | 결제·인증·정산·민감정보 도메인, 외부 감사 대상, 장애 영향도가 큰 코드 |
| 중 | `specs/중/` | 정식 도메인 본 기능 (사용자/펫/예약/주문 등) — 기본 적용 |
| 하 | `specs/하/` | PoC, 단기 실험, 해커톤 단계 — 폐기 가능성 있는 코드 |

같은 도메인 안에서도 메서드 단위로 등급이 다를 수 있다. 그 경우 메서드 javadoc 또는 클래스 javadoc 에 적용 등급을 명시한다.

## 적용 대상 환경

세 등급 모두 다음 스택을 가정한다 (실제 `decapet-official/backend` 환경).

- Spring Boot 4.0.1, Java 17, Gradle
- JPA + Spring Data, Specification, QueryDSL 가용
- 패키지 루트: `com.backend`, 도메인 기반 구조 (`com.backend.domain.{x}`)
- DTO: Java `record`, Entity↔DTO 변환은 `@Component` 클래스 (`UserMapper` 패턴)
- 글로벌: `GlobalExceptionHandler` + `ErrorCode` enum + `BusinessException`, 공통응답 `SuccessResponse`
- 검증: `ValidationConstants`(정규식·메시지 상수) + `@Valid` + `@Pattern`
- ULID PK + `BaseEntity` (audit 필드), soft-delete `@SQLRestriction("deleted = false")`
- 인증: JWT(JJWT 0.12.x) + Bucket4j 레이트리밋 + 다중 Filter(`JwtFilter` → `AccountValidationFilter` → `PermissionFilter`)
- Swagger: springdoc OpenAPI 3
- 테스트: Testcontainers + Flyway

## 등급 간 관계

- 세 등급의 문서는 **각각 독립적**이다. 한 등급 문서 안에 다른 등급을 참조하는 표현(예: "상 등급에서 완화한 부분")은 들어 있지 않다.
- 등급 차이는 강제 항목의 **범위와 강도**로 표현된다 — 상은 must 가 가장 많고, 하는 핵심만 must.
- 같은 영역(예: `controller-pattern`)이라도 등급별로 자체 완결된 규칙·예시·체크리스트를 가진다.

## 문서 영역 (등급별 동일 16개)

| 영역 | 파일명 |
| --- | --- |
| 기초 | `coding-standards.md`, `naming_rule.md`, `project-base-guidelines.md`, `package_rule.md`, `code-generate-guidelines.md` |
| 레이어 패턴 | `controller-pattern.md`, `service-pattern.md`, `mapper-pattern.md`, `query-patterns.md` |
| 횡단 관심사 | `exception-handling-pattern.md`, `domain-structure.md`, `springboot-standard.md` |
| 보안 / 품질 | `quality-rule.md`, `encryption-decryption-guidelines.md`, `masking-guildlines.md`, `spec-driven-buisness-rule-guidlines.md` |

## 변경 이력

- 2026-04-29: 3등급 모두 프로젝트 핏(JPA/SB4/com.backend)으로 신규 작성. 등급 간 참조 제거.
